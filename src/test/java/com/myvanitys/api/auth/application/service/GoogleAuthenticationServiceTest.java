package com.myvanitys.api.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
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
class GoogleAuthenticationServiceTest {

  @Mock
  private GoogleAuthClient googleAuthClient;

  @Mock
  private JwtTokenGeneratorAdapter tokenGenerator;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GoogleAuthenticationService target;

  private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/callback";

  @BeforeEach
  void setUp() {
    target = new GoogleAuthenticationService(googleAuthClient, userRepository, tokenGenerator);
    // Inject the redirect URI manually
    ReflectionTestUtils.setField(target, "defaultRedirectUri", DEFAULT_REDIRECT_URI);
  }

  @Nested
  class AuthenticateWithGoogle {

    @Test
    void when_givenCommandWithRedirectUri_then_returnsUserSession() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      // 1. Mocks required for googleAuthClient
      final String pictureUrl = "https://example.com/pic2.jpg";
      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", pictureUrl);

      // Use Mono.just() to wrap simulated response
      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), command.redirectUri()))
          .thenReturn(Mono.just(googleUserInfo));

      // 2. Simulation for userRepository - first find returns empty (user not found)
      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.empty());

      // 3. Simulation for userRepository - save returns the saved user
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            return Mono.just(userToSave);
          });

      // 4. Simulation of the token generator
      when(tokenGenerator.generateToken(any(TokenClaims.class)))
          .thenReturn("dummy-jwt-token");

      when(tokenGenerator.createClaimsFromUser(any(User.class)))
          .thenReturn(new TokenClaims("dummy-claim", "dummy-email", "dummy-name"));

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();

      // Then
      assertThat(result).isNotNull().satisfies(session -> {
        assertThat(session.token()).isEqualTo("dummy-jwt-token");
        assertThat(session.user().getEmail()).isEqualTo("user@example.com");
        assertThat(session.user().getAuthorizationId()).isEqualTo("google-user-id");
        assertThat(session.user().getName()).isEqualTo("Jane Doe");
      });

      // Verify that repository methods were called
      verify(userRepository).findByAuthorizationId("google-user-id");
      verify(userRepository).save(any(User.class));
    }

    @Test
    void when_givenCommandWithoutRedirectUri_then_usesDefaultRedirectUri() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", null);

      // Mocks required for googleAuthClient
      final String pictureUrl = "https://example.com/pic2.jpg";
      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", pictureUrl);

      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI))
          .thenReturn(Mono.just(googleUserInfo));

      // User repository mocks
      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.empty());

      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            return Mono.just(userToSave);
          });

      when(tokenGenerator.generateToken(any(TokenClaims.class)))
          .thenReturn("dummy-jwt-token");

      when(tokenGenerator.createClaimsFromUser(any(User.class)))
          .thenReturn(new TokenClaims("dummy-claim", "dummy-email", "dummy-name"));

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();

      // Then
      assertThat(result).isNotNull().satisfies(session -> {
        assertThat(session.token()).isEqualTo("dummy-jwt-token");
        assertThat(session.user().getEmail()).isEqualTo("user@example.com");
        assertThat(session.user().getAuthorizationId()).isEqualTo("google-user-id");
        assertThat(session.user().getName()).isEqualTo("Jane Doe");
      });

      // Verify default redirect URI was used
      verify(googleAuthClient).exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI);
    }

    @Test
    void when_userAlreadyExists_then_returnsExistingUser() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      // Mocks for Google client
      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");
      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), command.redirectUri()))
          .thenReturn(Mono.just(googleUserInfo));

      // Existing user in repository
      UUID existingUserId = UUID.randomUUID();
      User existingUser = new User(new EntityId(existingUserId), "google-user-id", "user@example.com", "Jane Doe");
      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.just(existingUser));

      when(userRepository.save(existingUser))
          .thenReturn(Mono.just(existingUser));

      // Token generation
      when(tokenGenerator.generateToken(any(TokenClaims.class)))
          .thenReturn("dummy-jwt-token");

      when(tokenGenerator.createClaimsFromUser(existingUser))
          .thenReturn(new TokenClaims(existingUserId.toString(), "user@example.com", "Jane Doe"));

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();

      // Then
      assertThat(result).isNotNull();
      assertThat(result.token()).isEqualTo("dummy-jwt-token");
      assertThat(result.user()).isSameAs(existingUser);

      // Verify repository interactions
      verify(userRepository).findByAuthorizationId("google-user-id");
      verify(userRepository).save(existingUser);
      verify(tokenGenerator).createClaimsFromUser(existingUser);
    }

    @Test
    void when_repositoryFailure_then_propagatesError() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", DEFAULT_REDIRECT_URI);

      // Mocks for the Google client
      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", "https://example.com/pic.jpg");
      when(googleAuthClient.exchangeCodeForUserInfo(command.code(), command.redirectUri()))
          .thenReturn(Mono.just(googleUserInfo));

      // Repository failure
      RuntimeException dbError = new RuntimeException("Database connection failed");
      when(userRepository.findByAuthorizationId("google-user-id"))
          .thenReturn(Mono.error(dbError));

      // When/Then
      StepVerifier.create(target.authenticateWithGoogle(command, requestId, flowId))
          .expectErrorMatches(error -> error == dbError)
          .verify();
    }
  }
}



