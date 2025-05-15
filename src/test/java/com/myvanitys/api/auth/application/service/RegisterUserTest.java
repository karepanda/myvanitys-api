package com.myvanitys.api.auth.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.domain.exception.AuthenticationFailedException;
import com.myvanitys.api.auth.domain.exception.UserAlreadyExistsException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.port.UserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RegisterUserTest {

  @Mock
  private GoogleAuthClient googleAuthClient;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenGenerator tokenGenerator;

  @InjectMocks
  private RegisterUser registerUser;

  private final String defaultRedirectUri = "https://www.myvanitys.com/callback";

  private final String authCode = "test-auth-code";

  private final UUID requestId = UUID.randomUUID();

  private final UUID flowId = UUID.randomUUID();

  private final String googleUserId = "google-user-id";

  private final String userEmail = "test@example.com";

  private final String userName = "Test User";

  private final String jwtToken = "jwt-token";

  private GoogleUserInfo googleUserInfo;

  private User user;

  private RegisterUserCommand command;

  private TokenClaims tokenClaims;

  @BeforeEach
  void setUp() {
    String pictureUrl = "https://example.com/picture.jpg";
    googleUserInfo = new GoogleUserInfo(googleUserId, userEmail, userName, pictureUrl);

    EntityId userId = EntityId.newId();
    Instant now = Instant.now();
    user = new User(userId, googleUserId, userEmail, userName, now);

    command = new RegisterUserCommand("google", authCode, defaultRedirectUri, now);

    // Crear TokenClaims según tu implementación actual
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(3600);
    tokenClaims = new TokenClaims(user, issuedAt, expiresAt);
  }

  @Test
  @DisplayName("Should successfully register a new user")
  void shouldRegisterNewUser() {
    // Arrange
    when(googleAuthClient.exchangeCodeForUserInfo(authCode, defaultRedirectUri))
        .thenReturn(Mono.just(googleUserInfo));
    when(userRepository.findByAuthorizationId(googleUserId))
        .thenReturn(Mono.empty());
    when(userRepository.save(any(User.class)))
        .thenReturn(Mono.just(user));
    when(tokenGenerator.createClaimsFromUser(any(User.class)))
        .thenReturn(tokenClaims);
    when(tokenGenerator.generateToken(any(TokenClaims.class)))
        .thenReturn(jwtToken);

    // Act & Assert
    StepVerifier.create(registerUser.execute(command, requestId, flowId))
        .expectNextMatches(result -> {
          UserSession session = result.session();
          return session.token().equals(jwtToken) &&
              session.user().getEmail().equals(userEmail) &&
              session.user().getName().equals(userName);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should throw UserAlreadyExistsException when user exists")
  void shouldThrowExceptionWhenUserExists() {
    // Arrange
    when(googleAuthClient.exchangeCodeForUserInfo(authCode, defaultRedirectUri))
        .thenReturn(Mono.just(googleUserInfo));
    when(userRepository.findByAuthorizationId(googleUserId))
        .thenReturn(Mono.just(user));

    // Act & Assert
    StepVerifier.create(registerUser.execute(command, requestId, flowId))
        .expectError(UserAlreadyExistsException.class)
        .verify();
  }

  @Test
  @DisplayName("Should throw AuthenticationFailedException for unsupported provider")
  void shouldThrowExceptionForUnsupportedProvider() {
    // Arrange
    RegisterUserCommand invalidCommand = new RegisterUserCommand(
        "facebook", authCode, defaultRedirectUri, Instant.now());

    // Act & Assert
    StepVerifier.create(registerUser.execute(invalidCommand, requestId, flowId))
        .expectError(AuthenticationFailedException.class)
        .verify();
  }

  @Test
  @DisplayName("Should throw AuthenticationFailedException when Google authentication fails")
  void shouldThrowExceptionWhenGoogleAuthFails() {
    // Arrange
    when(googleAuthClient.exchangeCodeForUserInfo(authCode, defaultRedirectUri))
        .thenReturn(Mono.error(new RuntimeException("Google API Error")));

    // Act & Assert
    StepVerifier.create(registerUser.execute(command, requestId, flowId))
        .expectError(AuthenticationFailedException.class)
        .verify();
  }
}