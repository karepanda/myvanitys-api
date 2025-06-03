package com.myvanitys.api.auth.application.service;

import com.myvanitys.api.auth.application.port.primary.RegisterUserUseCase;
import com.myvanitys.api.auth.application.port.primary.command.RegisterUserCommand;
import com.myvanitys.api.auth.application.port.primary.result.UserRegistrationResult;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUser implements RegisterUserUseCase {

  private final GoogleAuthClient googleAuthClient;

  private final UserRepository userRepository;

  private final TokenGenerator tokenGenerator;

  @Value("${google.oauth.redirect-uri:https://www.myvanitys.com/callback}")
  private String defaultRedirectUri;

  @Override
  public Mono<UserRegistrationResult> execute(RegisterUserCommand command, UUID requestId, UUID flowId) {
    log.info("Processing user registration. Provider: {}, RequestID: {}, FlowID: {}",
        command.provider(), requestId, flowId);

    if (!"google".equalsIgnoreCase(command.provider())) {
      return Mono.error(new AuthenticationFailedException(
          "Unsupported authentication provider: " + command.provider()));
    }

    String redirectUri = command.redirectUri() != null ? command.redirectUri() : defaultRedirectUri;

    return googleAuthClient.exchangeCodeForUserInfo(command.code(), redirectUri)
        .flatMap(googleUserInfo -> {
          log.info("Retrieved user info from Google. User ID: {}, Email: {}",
              googleUserInfo.id(), googleUserInfo.email());

          return userRepository.findByAuthorizationId(googleUserInfo.id())
              .flatMap(existingUser -> {
                log.warn("User with Google ID {} already exists", googleUserInfo.id());
                return Mono.<UserRegistrationResult>error(new UserAlreadyExistsException(
                    "User with Google ID " + googleUserInfo.id() + " already exists"));
              })
              .switchIfEmpty(Mono.defer(() -> createAndSaveUser(googleUserInfo, command.registrationDate())
                  .map(newUser -> {
                    TokenClaims claims = tokenGenerator.createClaimsFromUser(newUser);
                    String token = tokenGenerator.generateToken(claims);

                    UserSession session = new UserSession(token, newUser);

                    log.info("User registered successfully. User ID: {}, Email: {}",
                        newUser.getId(), newUser.getEmail());

                    return new UserRegistrationResult(session);
                  }))
              );
        })
        .onErrorResume(e -> {
          if (e instanceof AuthenticationFailedException || e instanceof UserAlreadyExistsException) {
            return Mono.error(e);
          } else {
            log.error("Error during user registration: {}", e.getMessage(), e);
            return Mono.error(
                new AuthenticationFailedException("Failed to register user: " + e.getMessage(), e));
          }
        });
  }

  private Mono<User> createAndSaveUser(GoogleUserInfo googleUserInfo, Instant registrationDate) {
    User newUser;

    if (registrationDate != null) {
      newUser = new User(
          EntityId.newId(),
          googleUserInfo.id(),
          googleUserInfo.email(),
          googleUserInfo.name(),
          registrationDate
      );
    } else {
      newUser = new User(
          EntityId.newId(),
          googleUserInfo.id(),
          googleUserInfo.email(),
          googleUserInfo.name()
      );
    }

    log.info("Creating new user: {}", newUser);
    return userRepository.save(newUser);
  }

}