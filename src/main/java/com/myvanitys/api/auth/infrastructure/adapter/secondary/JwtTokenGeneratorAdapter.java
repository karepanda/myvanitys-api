package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.auth.infrastructure.security.JwtClaimsAdapter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Component
public class JwtTokenGeneratorAdapter implements TokenGenerator {

  private final SecretKey key;

  private final long expirationInSeconds;

  private final String issuer;

  public JwtTokenGeneratorAdapter(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration:3600}") long expirationInSeconds,
      @Value("${jwt.issuer:myvanitys}") String issuer) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationInSeconds = expirationInSeconds;
    this.issuer = issuer;
  }

  @Override
  public String generateToken(TokenClaims claims) {
    Map<String, Object> jwtClaims = JwtClaimsAdapter.toJwtClaims(claims);

    return Jwts.builder()
        .subject(claims.user().getId().getValue().toString())
        .issuer(issuer)
        .issuedAt(Date.from(claims.issuedAt()))
        .expiration(Date.from(claims.expiresAt()))
        .claims(jwtClaims)
        .signWith(key)
        .compact();
  }

  @Override
  public TokenClaims createClaimsFromUser(User user) {
    return TokenClaims.fromUser(user, expirationInSeconds);
  }

  @Override
  public UUID extractUserId(String token) {
    try {
      Claims claims = parseToken(token);
      return UUID.fromString(claims.getSubject());
    } catch (JwtException e) {
      log.error("Error extracting user ID from token: {}", e.getMessage());
      throw new TokenException("Invalid token");
    }
  }

  @Override
  public TokenClaims validateToken(String token) {
    try {
      Claims jwtClaims = parseToken(token);
      Instant issuedAt = jwtClaims.getIssuedAt().toInstant();
      Instant expiresAt = jwtClaims.getExpiration().toInstant();

      return JwtClaimsAdapter.fromJwtClaims(jwtClaims, issuedAt, expiresAt);
    } catch (JwtException e) {
      log.error("Error validating token: {}", e.getMessage());
      throw new TokenException("Invalid token: " + e.getMessage());
    }
  }

  private Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}