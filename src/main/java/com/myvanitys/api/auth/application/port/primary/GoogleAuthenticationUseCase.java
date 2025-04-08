package com.myvanitys.api.auth.application.port.primary;

import com.myvanitys.api.auth.domain.model.User;

/**
 * Input port for authentication using Google OAuth. This port defines the operations that primary adapters can use to perform
 * authentication with Google.
 */
public interface GoogleAuthenticationUseCase {

  /**
   * Authenticates a user using a Google OAuth authorization code. This method exchanges the code for Google tokens, retrieves user
   * information, creates or updates the user in the database, and generates a JWT.
   *
   * @param authorizationCode The authorization code obtained from Google OAuth
   * @return The authentication result containing the JWT token and user information
   */
  AuthenticationResult authenticateWithGoogle(String authorizationCode);

  /**
   * Inner class that encapsulates the result of the authentication process.
   */
  record AuthenticationResult(String token, User user) {

  }
}
