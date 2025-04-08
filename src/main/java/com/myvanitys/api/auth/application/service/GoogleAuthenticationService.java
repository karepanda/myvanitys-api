package com.myvanitys.api.auth.application.service;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.GoogleAuthenticationUseCase;
import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.model.UserSession;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleAuthenticationService implements GoogleAuthenticationUseCase {

  @Override
  public UserSession authenticateWithGoogle(GoogleAuthCommand command, UUID requestId, UUID flowId) {
    log.info("Google authentication processing. RequestID: {}, FlowID: {}", requestId, flowId);
    log.info("Received authorization code: {}", command.code());

    EntityId userId = new EntityId(UUID.randomUUID());
    String googleUserId = "google-" + UUID.randomUUID().toString(); // ID de Google
    String email = "user@example.com";
    String name = "User test";

    User user = new User(userId, googleUserId, email, name);

    return new UserSession("jwt-token-" + UUID.randomUUID(), user);
  }
}