package com.myvanitys.api.auth.infrastructure.security;

import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.product.domain.valueobject.EntityId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class JwtClaimsAdapter {

  /**
   * Convert domain TokenClaims to a Map for JWT
   */
  public static Map<String, Object> toJwtClaims(TokenClaims claims) {
    Map<String, Object> jwtClaims = new HashMap<>();
    jwtClaims.put("sub", claims.user().getId().getValue().toString());
    jwtClaims.put("auth_id", claims.user().getAuthorizationId());
    jwtClaims.put("email", claims.user().getEmail());
    if (claims.user().getName() != null) {
      jwtClaims.put("name", claims.user().getName());
    }

    jwtClaims.put("iat", claims.issuedAt().getEpochSecond());
    jwtClaims.put("exp", claims.expiresAt().getEpochSecond());

    return jwtClaims;
  }

  /**
   * Convert JWT claims to domain TokenClaims
   */
  public static TokenClaims fromJwtClaims(Map<String, Object> jwtClaims, Instant issuedAt, Instant expiresAt) {
    String userId = (String) jwtClaims.get("sub");
    String authId = (String) jwtClaims.get("auth_id");
    String email = (String) jwtClaims.get("email");
    String name = (String) jwtClaims.get("name");

    User user = new User(
        new EntityId(UUID.fromString(userId)),
        authId != null ? authId : "unknown", // Podría ser null en tokens antiguos
        email,
        name,
        Instant.now()
    );

    return new TokenClaims(user, issuedAt, expiresAt);
  }
}