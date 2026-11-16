package com.myvanitys.api.common;

/**
 * Central place for redacting sensitive values before they reach log output.
 *
 * <p>Credentials (authorization codes, tokens) must never be logged at all; identifiers and
 * email addresses are reduced to opaque forms so logs stay diagnosable without exposing personal
 * or provider data.</p>
 */
public final class LogSanitizer {

  private static final String MASK = "***";

  private LogSanitizer() {
  }

  /**
   * Fully masks an identifier. Request correlation must use the request ID rather than a stable
   * fragment of a user or provider identifier.
   */
  public static String maskId() {
    return MASK;
  }

  /**
   * Masks the local part of an email address, retaining only the domain for operational context.
   */
  public static String maskEmail(String email) {
    if (email == null) {
      return MASK;
    }
    int at = email.indexOf('@');
    if (at <= 0) {
      return MASK;
    }
    return MASK + email.substring(at);
  }
}
