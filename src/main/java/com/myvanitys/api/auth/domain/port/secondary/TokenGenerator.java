package com.myvanitys.api.auth.domain.port.secondary;

import java.util.UUID;

import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;

public interface TokenGenerator {

  /**
   * Generate a token from the provided claims
   */
  String generateToken(TokenClaims claims);

  /**
   * Create claims from a user
   */
  TokenClaims createClaimsFromUser(User user);

  /**
   * Extract user ID from a token
   */
  UUID extractUserId(String token);

  /**
   * Validates a token and returns claims
   */
  TokenClaims validateToken(String token);

}
