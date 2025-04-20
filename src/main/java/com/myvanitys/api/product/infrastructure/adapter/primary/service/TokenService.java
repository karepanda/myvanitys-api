package com.myvanitys.api.product.infrastructure.adapter.primary.service;

import java.util.UUID;

import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  // For now, temporary implementation for testing
  public UUID extractUserId(String token) {
    // TODO: Implement true JWT decoding
    if ("4/P7q7W91".equals(token)) {
      return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }
    throw new UnauthorizedException("Invalid token");
  }
}
