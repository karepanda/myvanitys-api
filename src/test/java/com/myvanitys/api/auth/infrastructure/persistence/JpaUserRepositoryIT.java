package com.myvanitys.api.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class JpaUserRepositoryIT extends AbstractJpaAuthTest {

  @Test
  void should_SaveUser_When_ValidUserEntity() {
    // Given
    UserEntity user = UserEntity.builder()
        .userId(UUID.randomUUID())
        .token("google-auth-id-123")
        .email("test@example.com")
        .name("Test User")
        .createdAt(Instant.now())
        .build();

    // When
    UserEntity savedUser = userRepository.save(user);

    // Then
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getUserId()).isNotNull();
    assertThat(savedUser.getToken()).isEqualTo("google-auth-id-123");
    assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    assertThat(savedUser.getName()).isEqualTo("Test User");
    assertThat(savedUser.getCreatedAt()).isNotNull();
    assertThat(savedUser.getVersion()).isNotNull(); // 💡
  }

  @Test
  void should_FindUserByToken_When_UserExists() {
    // Given
    UserEntity user = UserEntity.builder()
        .userId(UUID.randomUUID())
        .token("google-auth-id-123")
        .email("test@example.com")
        .name("Test User")
        .createdAt(Instant.now())
        .build();
    userRepository.save(user);

    // When
    UserEntity foundUser = userRepository.findByToken("google-auth-id-123");

    // Then
    assertThat(foundUser).isNotNull();
    assertThat(foundUser.getToken()).isEqualTo("google-auth-id-123");
    assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void should_ReturnNull_When_TokenNotFound() {
    // When
    UserEntity foundUser = userRepository.findByToken("non-existent-token");

    // Then
    assertThat(foundUser).isNull();
  }

  @Test
  void should_UpdateUser_When_UserExists() {
    // Given
    UserEntity user = UserEntity.builder()
        .userId(UUID.randomUUID())
        .token("google-auth-id-123")
        .email("test@example.com")
        .name("Test User")
        .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
        .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
        .build();
    userRepository.save(user);
    userRepository.flush();

    // When
    UserEntity userToUpdate = userRepository.findByToken("google-auth-id-123");
    userToUpdate.setEmail("updated@example.com");
    userToUpdate.setName("Updated Name");
    userToUpdate.setUpdatedAt(Instant.now());
    UserEntity updatedUser = userRepository.save(userToUpdate);
    userRepository.flush();

    // Then
    assertThat(updatedUser).isNotNull();
    assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    assertThat(updatedUser.getVersion()).isEqualTo(1L);

    // Verify the entity was actually updated in the database
    UserEntity retrievedUser = userRepository.findByToken("google-auth-id-123");
    assertThat(retrievedUser.getEmail()).isEqualTo("updated@example.com");
    assertThat(retrievedUser.getName()).isEqualTo("Updated Name");

  }

  @Test
  void should_HandleUniqueConstraint_When_DuplicateToken() {
    // Given
    UserEntity user1 = UserEntity.builder()
        .userId(UUID.randomUUID())
        .token("duplicate-token")
        .email("user1@example.com")
        .name("User 1")
        .createdAt(Instant.now())
        .build();
    userRepository.save(user1);
    userRepository.flush();

    // When & Then
    UserEntity user2 = UserEntity.builder()
        .userId(UUID.randomUUID())
        .token("duplicate-token") // same token
        .email("user2@example.com")
        .name("User 2")
        .updatedAt(Instant.now())
        .build();

    assertThatThrownBy(() -> {
      userRepository.save(user2);
      userRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

}
