package com.myvanitys.api.auth.application.port.primary;

import java.util.UUID;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.UserSession;

public interface GoogleAuthenticationUseCase {

  /**
   * Authenticates a user using the Google authorization code.
   *
   * @param command Command containing the authorization code
   * @param requestId Request ID for tracking
   * @param flowId Flow ID for tracking
   * @return User session with the token and user data
   */
  UserSession authenticateWithGoogle(GoogleAuthCommand command, UUID requestId, UUID flowId);
}