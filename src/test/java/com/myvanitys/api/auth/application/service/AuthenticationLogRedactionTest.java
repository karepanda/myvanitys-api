package com.myvanitys.api.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.auth.domain.port.secondary.UserRepository;
import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthenticationLogRedactionTest {

  private static final String SYNTHETIC_AUTH_CODE = "synthetic-secret-authorization-code";

  private static final String SYNTHETIC_EMAIL = "jane.doe@example.com";

  private static final String SYNTHETIC_GOOGLE_ID = "google-oauth-subject-123";

  @Mock
  private GoogleAuthClient googleAuthClient;

  @Mock
  private TokenGenerator tokenGenerator;

  @Mock
  private UserRepository userRepository;

  private GoogleAuthentication googleAuthentication;

  private ListAppender<ILoggingEvent> appender;

  private Logger logger;

  @BeforeEach
  void setUp() {
    googleAuthentication = new GoogleAuthentication(googleAuthClient, userRepository, tokenGenerator);
    ReflectionTestUtils.setField(googleAuthentication, "defaultRedirectUri", "http://localhost:5173/callback");

    logger = (Logger) LoggerFactory.getLogger(GoogleAuthentication.class);
    logger.setLevel(Level.INFO);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(appender);
  }

  @Test
  void shouldNotLogAuthorizationCodeOrEmail() {
    String redirectUri = "http://localhost:5173/callback";
    GoogleAuthCommand command = GoogleAuthCommand.of(SYNTHETIC_AUTH_CODE, redirectUri);
    GoogleUserInfo googleUserInfo = new GoogleUserInfo(SYNTHETIC_GOOGLE_ID, SYNTHETIC_EMAIL, "Jane Doe", "https://example.com/pic.jpg");

    User user = new User(new EntityId(UUID.randomUUID()), SYNTHETIC_GOOGLE_ID, SYNTHETIC_EMAIL, "Jane Doe");

    when(googleAuthClient.exchangeCodeForUserInfo(SYNTHETIC_AUTH_CODE, redirectUri))
        .thenReturn(Mono.just(googleUserInfo));
    when(userRepository.findByAuthorizationId(SYNTHETIC_GOOGLE_ID)).thenReturn(Mono.just(user));
    when(tokenGenerator.createClaimsFromUser(user)).thenReturn(null);
    when(tokenGenerator.generateToken(any())).thenReturn("synthetic-jwt");

    googleAuthentication.authenticateWithGoogle(command, UUID.randomUUID(), UUID.randomUUID()).block();

    String logged = appender.list.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .reduce("", (a, b) -> a + "\n" + b);

    assertThat(logged).doesNotContain(SYNTHETIC_AUTH_CODE);
    assertThat(logged).doesNotContain(SYNTHETIC_EMAIL);
    assertThat(logged).doesNotContain(SYNTHETIC_GOOGLE_ID);
    assertThat(logged).contains("***@example.com");
  }
}
