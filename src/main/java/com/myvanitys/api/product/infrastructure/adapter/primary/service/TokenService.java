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
      return UUID.fromString("01965972-7033-7950-9cb1-56fe1251e72e");
    }
    throw new UnauthorizedException("Invalid token");
  }
}
