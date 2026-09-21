package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

/**
 * Adapter-level integration test that drives the real {@link UserRepositoryAdapter} and the real
 * {@link JpaUserRepository} against a PostgreSQL 16 Testcontainer where Flyway owns the schema.
 *
 * <p>The adapter performs repository writes on {@code Schedulers.boundedElastic} and therefore outside any
 * test-managed transaction, so the writes below commit for real. Isolation relies on UUID-unique tokens/emails and on
 * deleting only the rows created by this class (never Flyway seed data).</p>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
    })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserRepositoryAdapterIT extends AbstractIntegrationTest {

  @Autowired
  private UserRepositoryAdapter userRepositoryAdapter;

  @Autowired
  private JpaUserRepository jpaUserRepository;

  private final Set<UUID> createdUserIds = new HashSet<>();

  @AfterEach
  void deleteUsersCreatedByThisTest() {
    createdUserIds.forEach(id -> jpaUserRepository.findById(id).ifPresent(jpaUserRepository::delete));
    createdUserIds.clear();
  }

  @Test
  @DisplayName("Given a new domain user when saved through the real adapter then a generated id is persisted with initial version 0 and null updatedAt")
  void givenNewUser_whenSavedThroughAdapter_thenPersistsGeneratedIdAndInitialVersion() {
    // Given
    Instant fixedCreateAt = Instant.parse("2023-05-10T08:30:15.250Z");
    String token = uniqueToken();
    String email = uniqueEmail();
    String name = "Adapter New User";
    User newUser = new User(null, token, email, name, fixedCreateAt);

    // When
    User returned = saveThroughAdapter(newUser);

    // Then
    assertThat(returned.getId()).isNotNull();
    assertThat(returned.getId().getValue()).isNotNull();
    assertThat(returned.getAuthorizationId()).isEqualTo(token);
    assertThat(returned.getEmail()).isEqualTo(email);
    assertThat(returned.getName()).isEqualTo(name);
    assertThat(returned.getCreateAt()).isEqualTo(fixedCreateAt);

    UUID generatedId = returned.getId().getValue();
    createdUserIds.add(generatedId);

    Optional<UserEntity> persisted = jpaUserRepository.findById(generatedId);
    assertThat(persisted).isPresent();
    persisted.ifPresent(entity -> {
      assertThat(entity.getUserId()).isEqualTo(generatedId);
      assertThat(entity.getToken()).isEqualTo(token);
      assertThat(entity.getEmail()).isEqualTo(email);
      assertThat(entity.getName()).isEqualTo(name);
      assertThat(entity.getCreatedAt()).isEqualTo(fixedCreateAt);
      assertThat(entity.getVersion()).isNotNull();
      assertThat(entity.getVersion()).isEqualTo(0L);
      assertThat(entity.getUpdatedAt()).isNull();
    });
  }

  @Test
  @DisplayName("Given a persisted user when updated through the real adapter then db createdAt is preserved, version increments and updatedAt is stamped")
  void givenPersistedUser_whenUpdatedThroughAdapter_thenPreservesCreatedAtAndIncrementsVersion() {
    // Given: insert through the adapter so persistence is exercised end-to-end
    Instant originalCreateAt = Instant.parse("2022-11-20T09:10:11.120Z");
    String token = uniqueToken();
    User original = new User(null, token, "original." + token + "@example.com", "Original Name", originalCreateAt);

    User inserted = saveThroughAdapter(original);
    UUID userId = inserted.getId().getValue();
    createdUserIds.add(userId);

    UserEntity afterInsert = jpaUserRepository.findById(userId)
        .orElseThrow(() -> new IllegalStateException("Inserted user was not persisted: " + userId));
    long originalVersion = afterInsert.getVersion();
    Instant persistedCreatedAt = afterInsert.getCreatedAt();

    // When: supply a deliberately different createAt to prove the database timestamp wins
    String updatedEmail = uniqueEmail();
    User update = new User(
        new EntityId(userId),
        token,
        updatedEmail,
        "Updated Name",
        Instant.parse("2000-01-01T00:00:00Z"));

    Instant before = Instant.now();
    StepVerifier.create(userRepositoryAdapter.save(update))
        .expectNextCount(1)
        .verifyComplete();
    Instant after = Instant.now();

    // Then
    UserEntity reloaded = jpaUserRepository.findById(userId)
        .orElseThrow(() -> new IllegalStateException("Updated user was not persisted: " + userId));
    assertThat(reloaded.getUserId()).isEqualTo(userId);
    assertThat(reloaded.getToken()).isEqualTo(token);
    assertThat(reloaded.getEmail()).isEqualTo(updatedEmail);
    assertThat(reloaded.getName()).isEqualTo("Updated Name");
    assertThat(reloaded.getCreatedAt()).isEqualTo(persistedCreatedAt);
    assertThat(reloaded.getUpdatedAt()).isNotNull();
    assertThat(reloaded.getUpdatedAt()).isBetween(before, after);
    assertThat(reloaded.getVersion()).isEqualTo(originalVersion + 1);
  }

  private User saveThroughAdapter(User user) {
    AtomicReference<User> saved = new AtomicReference<>();
    StepVerifier.create(userRepositoryAdapter.save(user))
        .assertNext(saved::set)
        .verifyComplete();
    return saved.get();
  }

  private String uniqueToken() {
    return "adapter-it-" + UUID.randomUUID();
  }

  private String uniqueEmail() {
    return "adapter-it-" + UUID.randomUUID() + "@example.com";
  }
}
