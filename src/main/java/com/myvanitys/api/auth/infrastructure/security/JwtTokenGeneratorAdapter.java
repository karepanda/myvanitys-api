package com.myvanitys.api.auth.infrastructure.security;

import java.security.Key;
import java.util.Date;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenGeneratorAdapter {

  private final Key jwtSigningKey;

  private final JwtProperties jwtProperties;

  public String generateToken(TokenClaims claims) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

    return Jwts.builder()
        .subject(claims.userId())
        .claim("email", claims.email())
        .claim("name", claims.name())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(jwtSigningKey)
        .compact();
  }

  //  Convenience method to create TokenClaims from a User
  public TokenClaims createClaimsFromUser(User user) {
    return new TokenClaims(
        user.getId().getValue().toString(),
        user.getEmail(),
        user.getName()
    );
  }
}