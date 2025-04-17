package com.myvanitys.api.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.infrastructure.security.JwtTokenGeneratorAdapter;
import com.myvanitys.api.auth.infrastructure.security.TokenClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticationServiceTest {

  @Mock
  private GoogleAuthClient googleAuthClient;

  @Mock
  private JwtTokenGeneratorAdapter tokenGenerator;

  @InjectMocks
  private GoogleAuthenticationService target;

  private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/callback";

  @BeforeEach
  void setUp() {

    target = new GoogleAuthenticationService(googleAuthClient, tokenGenerator);
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
      Mockito.lenient().when(googleAuthClient.exchangeCodeForUserInfo(command.code(), command.redirectUri()))
          .thenReturn(Mono.just(googleUserInfo));

      // 2. Simulation of the token generator
      when(tokenGenerator.generateToken(any(TokenClaims.class)))
          .thenReturn("dummy-jwt-token");

      // Use lenient to avoid the problem of stubbing with different User
      Mockito.lenient().when(tokenGenerator.createClaimsFromUser(any(User.class)))
          .thenReturn(new TokenClaims("dummy-claim", "dummy-email", "dummy-name"));

      // When
      UserSession result =
          target.authenticateWithGoogle(command, requestId, flowId).block();  // Block the Mono to obtain the result

      // Then
      assertThat(result).isNotNull().satisfies(session -> {
        assertThat(session.token()).isEqualTo("dummy-jwt-token");
        assertThat(session.email()).isEqualTo("user@example.com");
        assertThat(session.googleId()).isEqualTo("google-user-id");
        assertThat(session.name()).isEqualTo("Jane Doe");
      });
    }

    @Test
    void when_givenCommandWithoutRedirectUri_then_usesDefaultRedirectUri() {
      // Given
      UUID requestId = UUID.randomUUID();
      UUID flowId = UUID.randomUUID();
      GoogleAuthCommand command = new GoogleAuthCommand("authorization-code", null); // redirectUri es null

      // Mocks required for googleAuthClient
      final String pictureUrl = "https://example.com/pic2.jpg";
      GoogleUserInfo googleUserInfo = new GoogleUserInfo("google-user-id", "user@example.com", "Jane Doe", pictureUrl);

      // Use Mono.just() to wrap simulated response
      Mockito.lenient().when(
              googleAuthClient.exchangeCodeForUserInfo(command.code(), DEFAULT_REDIRECT_URI)) // Esperamos que use DEFAULT_REDIRECT_URI aquí
          .thenReturn(Mono.just(googleUserInfo)); // Wrap in Mono.just()

      when(tokenGenerator.generateToken(any(TokenClaims.class)))
          .thenReturn("dummy-jwt-token");

      // Use lenient to avoid the problem of stubbing with different User
      Mockito.lenient().when(tokenGenerator.createClaimsFromUser(any(User.class)))
          .thenReturn(new TokenClaims("dummy-claim", "dummy-email", "dummy-name"));

      // When
      UserSession result = target.authenticateWithGoogle(command, requestId, flowId).block();  // Bloquear el Mono para obtener el resultado

      // Then
      assertThat(result).isNotNull().satisfies(session -> {
        assertThat(session.token()).isEqualTo("dummy-jwt-token");
        assertThat(session.email()).isEqualTo("user@example.com");
        assertThat(session.googleId()).isEqualTo("google-user-id");
        assertThat(session.name()).isEqualTo("Jane Doe");
      });
    }

  }


}



