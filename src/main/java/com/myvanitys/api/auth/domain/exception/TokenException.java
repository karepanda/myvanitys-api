package com.myvanitys.api.auth.domain.exception;

import com.myvanitys.api.common.DomainException;


public class TokenException extends DomainException {

  public TokenException(String message) {
    super(message);
  }

  public TokenException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an exception for invalid token
   */
  public static TokenException invalidToken(String reason) {
    return new TokenException("Invalid token: " + reason);
  }

  /**
   * Creates an exception for an expired token
   */
  public static TokenException expiredToken() {
    return new TokenException("Token has expired");
  }

  /**
   * Crea una excepción para claims inválidas
   */
  public static TokenException invalidClaims(String details) {
    return new TokenException("Invalid token claims: " + details);
  }
}