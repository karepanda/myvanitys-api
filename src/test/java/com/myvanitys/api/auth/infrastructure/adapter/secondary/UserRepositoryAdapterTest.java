package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.persistence.entity.UserEntity;
import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

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
            final UUID userId = UUID.randomUUID();
            final String authorizationId = "auth123";
            final String email = "test@example.com";
            final String name = "Test User";

            final User inputUser = new User(new EntityId(userId), authorizationId, email, name);

            final UserEntity savedEntity = new UserEntity();
            savedEntity.setUserId(userId);
            savedEntity.setEmail(email);
            savedEntity.setName(name);
            savedEntity.setToken(authorizationId);

            when(jpaUserRepository.save(savedEntity)).thenReturn(savedEntity);

            final Mono<User> result = target.save(inputUser);

            StepVerifier.create(result)
                    .assertNext(user -> {
                        assertThat(user.getId().getValue()).isEqualTo(userId);
                        assertThat(user.getAuthorizationId()).isEqualTo(authorizationId);
                        assertThat(user.getEmail()).isEqualTo(email);
                        assertThat(user.getName()).isEqualTo(name);
                    })
                    .verifyComplete();
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

            final UserEntity foundEntity = new UserEntity();
            foundEntity.setUserId(userId);
            foundEntity.setEmail(email);
            foundEntity.setName(name);
            foundEntity.setToken(authorizationId);

            when(jpaUserRepository.findByToken(authorizationId)).thenReturn(foundEntity);

            final Mono<User> result = target.findByAuthorizationId(authorizationId);

            StepVerifier.create(result)
                    .assertNext(user -> {
                        assertThat(user.getId().getValue()).isEqualTo(userId);
                        assertThat(user.getAuthorizationId()).isEqualTo(authorizationId);
                        assertThat(user.getEmail()).isEqualTo(email);
                        assertThat(user.getName()).isEqualTo(name);
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
