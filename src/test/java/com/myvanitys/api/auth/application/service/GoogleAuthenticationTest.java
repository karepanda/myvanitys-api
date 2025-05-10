package com.myvanitys.api.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.exception.UserNotFoundException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.port.UserRepository;
import com.myvanitys.api.auth.infrastructure.security.JwtTokenGeneratorAdapter;
import com.myvanitys.api.auth.infrastructure.security.TokenClaims;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticationTest {

  @Mock
  private GoogleAuthClient googleAuthClient;

  @Mock
  private JwtTokenGeneratorAdapter tokenGenerator;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GoogleAuthentication target;

  private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/callback";

  @BeforeEach
  void setUp() {
    target = new GoogleAuthentication(googleAuthClient, userRepository, tokenGenerator);
    ReflectionTestUtils.setField(target, "defaultRedirectUri", DEFAULT_REDIRECT_URI);
  }

  @Nested
  class AuthenticateWithGoogle {

    @Test
    void when_userExists_then_returnsUserSession() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");
      EntityId userId = new EntityId(UUID.randomUUID());
      User existingUser = new User(userId, "google-user-id", "user@example.com", "Jane Doe");

      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI))
          .thenReturn(Mono.just(googleUserInfo));

      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.just(existingUser));

      when(tokenGenerator.createClaimsFromUser(existingUser))
          .thenReturn(new TokenClaims(userId.toString(), "user@example.com", "Jane Doe"));

      when(tokenGenerator.generateToken(any()))
          .thenReturn("dummy-jwt-token");

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();

      // Then
      assertThat(result).isNotNull();
      assertThat(result.token()).isEqualTo("dummy-jwt-token");
      assertThat(result.user()).isSameAs(existingUser);

      verify(userRepository).findByAuthorizationId("google-user-id");
      verify(tokenGenerator).createClaimsFromUser(existingUser);
      verify(tokenGenerator).generateToken(any());
    }

    @Test
    void when_userNotFound_then_throwsUserNotFoundException() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");

      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI))
          .thenReturn(Mono.just(googleUserInfo));

      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.empty());

      // When / Then
      StepVerifier.create(target.authenticateWithGoogle(command, requestId, flowId))
          .expectErrorSatisfies(error -> {
            assertThat(error).isInstanceOf(UserNotFoundException.class);
            assertThat(error).hasMessageContaining("user@example.com");
          })
          .verify();

      verify(userRepository).findByAuthorizationId("google-user-id");
    }

    @Test
    void when_givenCommandWithoutRedirectUri_then_usesDefaultRedirectUri() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", null);

      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");
      EntityId userId = new EntityId(UUID.randomUUID());
      User user = new User(userId, "google-user-id", "user@example.com", "Jane Doe");

      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI))
          .thenReturn(Mono.just(googleUserInfo));

      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.just(user));

      when(tokenGenerator.createClaimsFromUser(user))
          .thenReturn(new TokenClaims(userId.toString(), "user@example.com", "Jane Doe"));

      when(tokenGenerator.generateToken(any()))
          .thenReturn("dummy-jwt-token");

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();

      // Then
      assertThat(result).isNotNull();
      assertThat(result.token()).isEqualTo("dummy-jwt-token");
      assertThat(result.user()).isSameAs(user);

      verify(googleAuthClient).exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI);
      verify(userRepository).findByAuthorizationId("google-user-id");
    }

    @Test
    void when_repositoryThrowsError_then_propagatesError() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");

      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI))
          .thenReturn(Mono.just(googleUserInfo));

      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.error(new RuntimeException("DB error")));

      // When / Then
      StepVerifier.create(target.authenticateWithGoogle(command, requestId, flowId))
          .expectErrorMatches(e -> e.getMessage().equals("DB error"))
          .verify();
    }
  }
}
