package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
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
}
