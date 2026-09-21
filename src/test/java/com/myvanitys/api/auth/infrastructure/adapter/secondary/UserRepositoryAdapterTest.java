package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class UserRepositoryAdapterTest {

  @Mock
  private JpaUserRepository jpaUserRepository;

  @InjectMocks
  private UserRepositoryAdapter target;

  @Nested
  class Save {

    @Test
    void when_validUser_then_returnsSavedUser() {
      // Arrange
      UUID userId = UUID.randomUUID();
      String authorizationId = "auth123";
      String email = "test@example.com";
      String name = "Test User";
      Instant createAt = Instant.now(); // Importante: creamos una fecha explícita

      // Usamos el constructor completo con createAt
      User inputUser = new User(new EntityId(userId), authorizationId, email, name, createAt);

      UserEntity savedEntity = new UserEntity();
      savedEntity.setUserId(userId);
      savedEntity.setEmail(email);
      savedEntity.setName(name);
      savedEntity.setToken(authorizationId);
      savedEntity.setCreatedAt(createAt); // Usamos la misma fecha para mantener consistencia
      savedEntity.setVersion(0L);

      ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

      // Act
      Mono<User> result = target.save(inputUser);

      // Assert
      StepVerifier.create(result)
          .assertNext(user -> {
            assertThat(user.getId().getValue()).isEqualTo(userId);
            assertThat(user.getAuthorizationId()).isEqualTo(authorizationId);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getCreateAt()).isEqualTo(createAt); // Verificamos la fecha
          })
          .verifyComplete();

      // Validate repository interaction
      verify(jpaUserRepository).save(captor.capture());

      UserEntity captured = captor.getValue();
      assertThat(captured.getUserId()).isEqualTo(userId);
      assertThat(captured.getToken()).isEqualTo(authorizationId);
      assertThat(captured.getEmail()).isEqualTo(email);
      assertThat(captured.getName()).isEqualTo(name);
      assertThat(captured.getCreatedAt()).isEqualTo(createAt); // Verificamos que la fecha sea la misma

      // Para la versión, permite null (si no se ha establecido) o 0L (si se estableció)
      assertThat(captured.getVersion() == null || captured.getVersion() == 0L).isTrue();
    }

  }

  @Nested
  class FindByAuthorizationId {

    @Test
    void when_existingAuthorizationId_then_returnsUser() {
      final UUID userId = UUID.randomUUID();
      final String authorizationId = "auth123";
      final String email = "test@example.com";
      final String name = "Test User";
      final Instant createAt = Instant.now(); // Añadir fecha de creación

      final UserEntity foundEntity = new UserEntity();
      foundEntity.setUserId(userId);
      foundEntity.setEmail(email);
      foundEntity.setName(name);
      foundEntity.setToken(authorizationId);
      foundEntity.setCreatedAt(createAt); // Establecer fecha de creación

      when(jpaUserRepository.findByToken(authorizationId)).thenReturn(foundEntity);

      final Mono<User> result = target.findByAuthorizationId(authorizationId);

      StepVerifier.create(result)
          .assertNext(user -> {
            assertThat(user.getId().getValue()).isEqualTo(userId);
            assertThat(user.getAuthorizationId()).isEqualTo(authorizationId);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getCreateAt()).isEqualTo(createAt); // Verificar la fecha
          })
          .verifyComplete();
    }

    @Test
    void when_nonExistingAuthorizationId_then_returnsEmptyMono() {
      final String authorizationId = "nonexistent";

      when(jpaUserRepository.findByToken(authorizationId)).thenReturn(null);

      final Mono<User> result = target.findByAuthorizationId(authorizationId);

      StepVerifier.create(result)
          .verifyComplete();
    }
  }

  @Nested
  class SaveNewUser {

    @Test
    void when_userIdIsNull_then_generatesIdAndPersistsSuppliedFieldsWithCreateAt() {
      // Arrange
      final Instant fixedCreateAt = Instant.parse("2023-05-10T08:30:15.250000Z");
      final String authorizationId = "google-new-user-token";
      final String email = "new.user@example.com";
      final String name = "New User";
      final User inputUser = new User(null, authorizationId, email, name, fixedCreateAt);

      when(jpaUserRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Mono<User> result = target.save(inputUser);

      // Assert: the mapping happens synchronously, the repository write happens on subscription
      final AtomicReference<User> savedUserRef = new AtomicReference<>();
      StepVerifier.create(result)
          .assertNext(savedUserRef::set)
          .verifyComplete();

      final User savedUser = savedUserRef.get();
      assertThat(savedUser.getId()).isNotNull();
      assertThat(savedUser.getId().getValue()).isNotNull();
      assertThat(savedUser.getAuthorizationId()).isEqualTo(authorizationId);
      assertThat(savedUser.getEmail()).isEqualTo(email);
      assertThat(savedUser.getName()).isEqualTo(name);
      assertThat(savedUser.getCreateAt()).isEqualTo(fixedCreateAt);

      final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(jpaUserRepository).save(captor.capture());
      verify(jpaUserRepository, never()).findById(any());

      final UserEntity captured = captor.getValue();
      assertThat(captured.getUserId()).isEqualTo(savedUser.getId().getValue());
      assertThat(captured.getToken()).isEqualTo(authorizationId);
      assertThat(captured.getEmail()).isEqualTo(email);
      assertThat(captured.getName()).isEqualTo(name);
      assertThat(captured.getCreatedAt()).isEqualTo(fixedCreateAt);
      assertThat(captured.getUpdatedAt()).isNull();
      assertThat(captured.getVersion()).isNull();
    }
  }

  @Nested
  class SaveExistingUser {

    private static final UUID USER_ID = UUID.fromString("6f1c1c34-9c4f-4f4a-8d5b-6a1c1f6b6a10");
    private static final Instant DB_CREATED_AT = Instant.parse("2022-01-01T00:00:00Z");
    private static final Instant DB_UPDATED_AT = Instant.parse("2022-06-01T00:00:00Z");
    private static final long DB_VERSION = 7L;

    @Test
    void when_existingEntity_then_copiesDbVersionAndCreatedAtAndStampsUpdatedAt() {
      // Arrange
      final UserEntity existingEntity = existingEntity();
      when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));
      when(jpaUserRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

      final Instant divergingCreateAt = Instant.parse("2020-01-01T00:00:00Z");
      final User updatedUser = new User(
          new EntityId(USER_ID), "updated-token", "updated@example.com", "Updated Name", divergingCreateAt);

      // Act: mapToEntity runs synchronously inside save(...), before the returned Mono is subscribed
      final Instant before = Instant.now();
      final Mono<User> result = target.save(updatedUser);
      final Instant after = Instant.now();

      StepVerifier.create(result)
          .expectNextCount(1)
          .verifyComplete();

      // Assert
      final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(jpaUserRepository).findById(USER_ID);
      verify(jpaUserRepository).save(captor.capture());

      final UserEntity captured = captor.getValue();
      assertThat(captured.getUserId()).isEqualTo(USER_ID);
      assertThat(captured.getVersion()).isEqualTo(DB_VERSION);
      assertThat(captured.getCreatedAt()).isEqualTo(DB_CREATED_AT);
      assertThat(captured.getUpdatedAt()).isNotNull();
      assertThat(captured.getUpdatedAt()).isBetween(before, after);
      assertThat(captured.getToken()).isEqualTo("updated-token");
      assertThat(captured.getEmail()).isEqualTo("updated@example.com");
      assertThat(captured.getName()).isEqualTo("Updated Name");
    }

    private UserEntity existingEntity() {
      final UserEntity entity = new UserEntity();
      entity.setUserId(USER_ID);
      entity.setToken("old-token");
      entity.setEmail("old@example.com");
      entity.setName("Old Name");
      entity.setVersion(DB_VERSION);
      entity.setCreatedAt(DB_CREATED_AT);
      entity.setUpdatedAt(DB_UPDATED_AT);
      return entity;
    }
  }

  @Nested
  class SaveExistingUserIdNotFound {

    private static final UUID USER_ID = UUID.fromString("2b9f5f90-3d2a-4ce2-9c3c-1c9f0b2d9f20");
    private static final Instant SUPPLIED_CREATE_AT = Instant.parse("2024-02-02T12:00:00.500000Z");

    @Test
    void when_idNotFound_then_persistsSuppliedDataAndLogsWarning(CapturedOutput output) {
      // Arrange
      final User inputUser = new User(
          new EntityId(USER_ID), "missing-token", "missing@example.com", "Missing User", SUPPLIED_CREATE_AT);

      when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.empty());
      when(jpaUserRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Mono<User> result = target.save(inputUser);

      StepVerifier.create(result)
          .expectNextCount(1)
          .verifyComplete();

      // Assert
      final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(jpaUserRepository).findById(USER_ID);
      verify(jpaUserRepository).save(captor.capture());

      final UserEntity captured = captor.getValue();
      assertThat(captured.getUserId()).isEqualTo(USER_ID);
      assertThat(captured.getToken()).isEqualTo("missing-token");
      assertThat(captured.getEmail()).isEqualTo("missing@example.com");
      assertThat(captured.getName()).isEqualTo("Missing User");
      assertThat(captured.getVersion()).isNull();
      assertThat(captured.getUpdatedAt()).isNull();
      assertThat(captured.getCreatedAt()).isEqualTo(SUPPLIED_CREATE_AT);

      // Stable log fragments only: no timestamps, no logger formatting, no full line
      assertThat(output.getOut()).contains("Attempting to update non-existent entity");
      assertThat(output.getOut()).contains(USER_ID.toString());
    }
  }

  @Nested
  class SaveError {

    private static final UUID USER_ID = UUID.fromString("9d7e4c10-5a6b-4f7c-8e2d-3b1a0c9f8e40");

    @Test
    void when_repositoryThrowsOptimisticLockingFailure_then_sameExceptionPropagates() {
      // Arrange
      final UserEntity existingEntity = new UserEntity();
      existingEntity.setUserId(USER_ID);
      existingEntity.setToken("locked-token");
      existingEntity.setEmail("locked@example.com");
      existingEntity.setName("Locked User");
      existingEntity.setVersion(3L);
      existingEntity.setCreatedAt(Instant.parse("2021-03-03T03:03:03Z"));

      when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));

      final ObjectOptimisticLockingFailureException failure =
          new ObjectOptimisticLockingFailureException(UserEntity.class, USER_ID);
      when(jpaUserRepository.save(any(UserEntity.class))).thenThrow(failure);

      final User inputUser = new User(
          new EntityId(USER_ID), "locked-token", "locked@example.com", "Locked User", Instant.parse("2021-03-03T03:03:03Z"));

      // Act
      final Mono<User> result = target.save(inputUser);

      // Assert
      StepVerifier.create(result)
          .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
          .verify();

      verify(jpaUserRepository).findById(USER_ID);
      verify(jpaUserRepository).save(any(UserEntity.class));
    }
  }
}
