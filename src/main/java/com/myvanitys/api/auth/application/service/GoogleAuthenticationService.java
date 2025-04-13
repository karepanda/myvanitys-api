// application/service/GoogleAuthenticationService.java

package com.myvanitys.api.auth.application.service;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.infrastructure.security.JwtTokenGeneratorAdapter;
import com.myvanitys.api.auth.infrastructure.security.TokenClaims;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService implements GoogleAuthenticationUseCase {

  private final GoogleAuthClient googleAuthClient;

  private final JwtTokenGeneratorAdapter tokenGenerator;

  @Value("${google.oauth.redirect-uri:http://localhost:5173/callback}")
  private String defaultRedirectUri;

  @Override
  public UserSession authenticateWithGoogle(GoogleAuthCommand command, UUID requestId, UUID flowId) {
    log.info("Processing Google authentication. RequestID: {}, FlowID: {}", requestId, flowId);
    log.info("Received authorization code: {}", command.code());

    // 1. Exchange code for user info
    String redirectUri = command.redirectUri() != null ? command.redirectUri() : defaultRedirectUri;
    GoogleUserInfo googleUserInfo = googleAuthClient.exchangeCodeForUserInfo(command.code(), redirectUri);

    log.info("Retrieved user info from Google. User ID: {}, Email: {}", googleUserInfo.id(), googleUserInfo.email());

    // 2. Create user (In a real implementation, you would check if the user exists first)
    EntityId userId = new EntityId(UUID.randomUUID());
    User user = new User(userId, googleUserInfo.id(), googleUserInfo.email(), googleUserInfo.name());

    // 3. Generate JWT token using TokenClaims
    TokenClaims claims = tokenGenerator.createClaimsFromUser(user);
    String token = tokenGenerator.generateToken(claims);

    return new UserSession(token, user);
  }
}