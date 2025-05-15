package com.myvanitys.api.auth.domain.model;

import java.time.Instant;

import com.myvanitys.api.auth.domain.exception.TokenException;

/**
 * Represents the claims of a token in the domain. It is a Value Object that encapsulates the authentication information.
 */
public record TokenClaims(
    User user,
    Instant issuedAt,
    Instant expiresAt
) {

  public TokenClaims {
    if (user == null) {
      throw new TokenException("user cannot be null");
    }
    if (issuedAt == null) {
      throw new TokenException("issuedAt cannot be null");
    }
    if (expiresAt == null) {
      throw new TokenException("expiresAt cannot be null");
    }
    if (expiresAt.isBefore(issuedAt)) {
      throw new TokenException("expiresAt cannot be before issuedAt");
    }
  }

  /**
   * Factory method to create claims from a User
   */
  public static TokenClaims fromUser(User user, long expirationInSeconds) {
    Instant now = Instant.now();
    return new TokenClaims(
        user,
        now,
        now.plusSeconds(expirationInSeconds)
    );
  }
}