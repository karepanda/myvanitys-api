package com.myvanitys.api.product.infrastructure.adapter.primary.service;

import java.util.UUID;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final TokenGenerator tokenGenerator;

  /**
   * Extracts the user ID from a JWT token.
   *
   * @param tokenHeader The JWT token to validate
   * @return The UUID of the user extracted from the token
   * @throws UnauthorizedException If the token is invalid or has expired
   */
  public UUID extractUserId(String tokenHeader) {
    String token = removeBearer(tokenHeader);
    try {
      return tokenGenerator.extractUserId(token);
    } catch (TokenException e) {
      log.warn("Token validation failed: {}", e.getMessage());
      throw new UnauthorizedException("Unauthorized: " + e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error validating token: {}", e.getMessage(), e);
      throw new UnauthorizedException("Authentication error");
    }
  }

  /**
   * Validates a JWT token.
   *
   * @param tokenHeader The JWT token to validate
   * @return true if the token is valid, false otherwise
   */
  public boolean isValidToken(String tokenHeader) {
    String token = removeBearer(tokenHeader);
    try {
      tokenGenerator.validateToken(token);
      return true;
    } catch (Exception e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  private String removeBearer(String tokenHeader) {
    if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
      return tokenHeader.substring(7);  // "Bearer ".length() == 7
    }
    return tokenHeader;
  }
}