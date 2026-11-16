package com.myvanitys.api.auth.application.service;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.exception.UserNotFoundException;
import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.auth.domain.port.secondary.UserRepository;
import com.myvanitys.api.common.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthentication implements GoogleAuthenticationUseCase {

  private final GoogleAuthClient googleAuthClient;

  private final UserRepository userRepository;

  private final TokenGenerator tokenGenerator;

  @Value("${google.oauth.redirect-uri:https://www.myvanitys.com/callback}")
  private String defaultRedirectUri;

  @Override
  public Mono<UserSession> authenticateWithGoogle(GoogleAuthCommand command, UUID requestId, UUID flowId) {
    log.info("Processing Google authentication. RequestID: {}, FlowID: {}", requestId, flowId);

    String redirectUri = command.redirectUri() != null ? command.redirectUri() : defaultRedirectUri;

    return googleAuthClient.exchangeCodeForUserInfo(command.code(), redirectUri)
        .flatMap(googleUserInfo -> {
          log.info("Retrieved user info from Google. User ID: {}, Email: {}",
              LogSanitizer.maskId(), LogSanitizer.maskEmail(googleUserInfo.email()));

          return userRepository.findByAuthorizationId(googleUserInfo.id())
              .switchIfEmpty(Mono.error(new UserNotFoundException("User not registered in the system")))
              .flatMap(user -> {
                // Generate JWT token
                TokenClaims claims = tokenGenerator.createClaimsFromUser(user);
                String token = tokenGenerator.generateToken(claims);
                return Mono.just(new UserSession(token, user));
              });
        });
  }

}