package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.common.valueobject.EntityId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real, unmocked jjwt/Jackson serialization, HMAC signing and signature-verification
 * path (jjwt-jackson + BOM-managed Jackson 2). Nothing jjwt-related is mocked here on purpose - this
 * is the guardrail that fails if Jackson 2 ever disappears from the runtime classpath.
 */
class JwtTokenGeneratorAdapterTest {

  private static final String SECRET = "myvanitys-test-secret-key-that-is-at-least-32-bytes-long!!";
  private static final String OTHER_SECRET = "another-rogue-test-secret-key-at-least-32-bytes-long!!";
  private static final long EXPIRATION_SECONDS = 3600L;
  private static final String ISSUER = "myvanitys";

  private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final String AUTHORIZATION_ID = "google-oauth2|105234567890123456789";
  private static final String EMAIL = "ada.lovelace@example.com";
  private static final String NAME = "Ada Lovelace";

  private static final Instant ISSUED_AT = Instant.parse("2026-01-15T10:15:30Z");
  private static final Instant EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");

  private static final Instant EXPIRED_ISSUED_AT = Instant.parse("2020-01-01T00:00:00Z");
  private static final Instant EXPIRED_EXPIRES_AT = Instant.parse("2020-01-01T01:00:00Z");

  private final JwtTokenGeneratorAdapter adapter =
      new JwtTokenGeneratorAdapter(SECRET, EXPIRATION_SECONDS, ISSUER);

  private final SecretKey verificationKey =
      Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

  private static User sampleUser() {
    return new User(new EntityId(USER_ID), AUTHORIZATION_ID, EMAIL, NAME);
  }

  private static TokenClaims validClaims() {
    return new TokenClaims(sampleUser(), ISSUED_AT, EXPIRES_AT);
  }

  @Test
  void generateToken_whenDeterministicClaims_thenProducesSignedJwtWithExactClaims() {
    // Given
    final TokenClaims claims = validClaims();

    // When
    final String token = adapter.generateToken(claims);

    // Then - the token is structurally a compact JWS: header.payload.signature
    assertThat(token.split("\\.")).hasSize(3);

    // And - it is independently verified with the configured HMAC key through the real JJWT parser
    final Jws<Claims> jws = Jwts.parser()
        .verifyWith(verificationKey)
        .build()
        .parseSignedClaims(token);
    assertThat(jws.getHeader().getAlgorithm()).startsWith("HS");
    // And - every security-sensitive claim is asserted independently
    final Claims parsed = jws.getPayload();
    assertThat(parsed.getSubject()).isEqualTo(USER_ID.toString());
    assertThat(parsed.getIssuer()).isEqualTo(ISSUER);
    assertThat(parsed.get("auth_id")).isEqualTo(AUTHORIZATION_ID);
    assertThat(parsed.get("email")).isEqualTo(EMAIL);
    assertThat(parsed.get("name")).isEqualTo(NAME);
    assertThat(parsed.getIssuedAt().toInstant()).isEqualTo(ISSUED_AT);
    assertThat(parsed.getExpiration().toInstant()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void validateToken_whenValidToken_thenReturnsTokenClaimsPreservingEveryField() {
    // Given
    final String token = adapter.generateToken(validClaims());

    // When
    final TokenClaims validated = adapter.validateToken(token);

    // Then - fields are asserted independently because User.equals() compares only the id
    assertThat(validated.user().getId().getValue()).isEqualTo(USER_ID);
    assertThat(validated.user().getAuthorizationId()).isEqualTo(AUTHORIZATION_ID);
    assertThat(validated.user().getEmail()).isEqualTo(EMAIL);
    assertThat(validated.user().getName()).isEqualTo(NAME);
    assertThat(validated.issuedAt()).isEqualTo(ISSUED_AT);
    assertThat(validated.expiresAt()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void extractUserId_whenValidToken_thenReturnsExactUserId() {
    // Given
    final String token = adapter.generateToken(validClaims());

    // Then
    assertThat(adapter.extractUserId(token)).isEqualTo(USER_ID);
  }

  @Test
  void createClaimsFromUser_whenCalled_thenUsesConfiguredExpirationFromNow() {
    // Given
    final User user = sampleUser();

    // When
    final Instant before = Instant.now();
    final TokenClaims claims = adapter.createClaimsFromUser(user);
    final Instant after = Instant.now();

    // Then - the same user instance is carried through
    assertThat(claims.user()).isSameAs(user);

    // And - issuedAt is bounded by the call window
    assertThat(claims.issuedAt()).isBetween(before, after);

    // And - expiresAt is exactly the configured duration after issuedAt
    assertThat(claims.expiresAt()).isEqualTo(claims.issuedAt().plusSeconds(EXPIRATION_SECONDS));
    assertThat(Duration.between(claims.issuedAt(), claims.expiresAt()).toSeconds())
        .isEqualTo(EXPIRATION_SECONDS);
  }

  @Test
  void validateToken_and_extractUserId_whenSignedWithDifferentKey_thenThrowTokenException() {
    // Given - a structurally valid token signed with another strong HMAC secret
    final JwtTokenGeneratorAdapter rogueAdapter =
        new JwtTokenGeneratorAdapter(OTHER_SECRET, EXPIRATION_SECONDS, ISSUER);
    final String rogueToken = rogueAdapter.generateToken(validClaims());

    // Then - the real signature verification rejects it in both public operations
    assertThatThrownBy(() -> adapter.validateToken(rogueToken))
        .isInstanceOf(TokenException.class)
        .hasMessageStartingWith("Invalid token:");
    assertThatThrownBy(() -> adapter.extractUserId(rogueToken))
        .isInstanceOf(TokenException.class)
        .hasMessage("Invalid token");
  }

  @Test
  void validateToken_and_extractUserId_whenTokenIsMalformed_thenThrowTokenException() {
    // Given
    final String malformedToken = "not-a-jwt";

    // Then - parsing fails in both public operations
    assertThatThrownBy(() -> adapter.validateToken(malformedToken))
        .isInstanceOf(TokenException.class)
        .hasMessageStartingWith("Invalid token:");
    assertThatThrownBy(() -> adapter.extractUserId(malformedToken))
        .isInstanceOf(TokenException.class)
        .hasMessage("Invalid token");
  }

  @Test
  void validateToken_and_extractUserId_whenTokenIsExpiredButWellSigned_thenThrowTokenException() {
    // Given - correct signature and format, but both instants are safely in the past
    final TokenClaims expiredClaims =
        new TokenClaims(sampleUser(), EXPIRED_ISSUED_AT, EXPIRED_EXPIRES_AT);
    final String expiredToken = adapter.generateToken(expiredClaims);

    // Then - only the expiration is rejected, proving expiry is enforced on a valid signature
    assertThatThrownBy(() -> adapter.validateToken(expiredToken))
        .isInstanceOf(TokenException.class)
        .hasMessageStartingWith("Invalid token:");
    assertThatThrownBy(() -> adapter.extractUserId(expiredToken))
        .isInstanceOf(TokenException.class)
        .hasMessage("Invalid token");
  }
}
