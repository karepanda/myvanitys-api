package com.myvanitys.api.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JwtClaimsAdapter}, focused on the security-sensitive JWT claim mapping.
 * Every security-relevant field is asserted independently; whole-map equality or User.equals()
 * are deliberately avoided because User.equals() compares only the id.
 */
class JwtClaimsAdapterTest {

  private static final UUID USER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final String AUTH_ID = "google-oauth2|105234567890123456789";
  private static final String EMAIL = "ada.lovelace@example.com";
  private static final String NAME = "Ada Lovelace";
  private static final Instant ISSUED_AT = Instant.parse("2026-01-15T10:15:30Z");
  private static final Instant EXPIRES_AT = Instant.parse("2026-01-15T11:15:30Z");

  private static User user(String name) {
    return new User(new EntityId(USER_UUID), AUTH_ID, EMAIL, name, ISSUED_AT);
  }

  @Test
  void toJwtClaims_whenNameIsPresent_thenMapsEverySecuritySensitiveClaimExactly() {
    // Given
    final TokenClaims claims = new TokenClaims(user(NAME), ISSUED_AT, EXPIRES_AT);

    // When
    final Map<String, Object> jwtClaims = JwtClaimsAdapter.toJwtClaims(claims);

    // Then - sub and auth_id are mapped without transformation
    assertThat(jwtClaims).containsEntry("sub", USER_UUID.toString());
    assertThat(jwtClaims).containsEntry("auth_id", AUTH_ID);

    // And the remaining claims are mapped exactly
    assertThat(jwtClaims).containsEntry("email", EMAIL);
    assertThat(jwtClaims).containsEntry("name", NAME);

    // And the temporal claims are epoch seconds, not millis or Instant objects
    assertThat(jwtClaims).containsEntry("iat", ISSUED_AT.getEpochSecond());
    assertThat(jwtClaims).containsEntry("exp", EXPIRES_AT.getEpochSecond());
  }

  @Test
  void toJwtClaims_whenNameIsNull_thenOmitsNameKeyButKeepsEveryOtherClaim() {
    // Given
    final TokenClaims claims = new TokenClaims(user(null), ISSUED_AT, EXPIRES_AT);

    // When
    final Map<String, Object> jwtClaims = JwtClaimsAdapter.toJwtClaims(claims);

    // Then - the name key is omitted entirely
    assertThat(jwtClaims).doesNotContainKey("name");

    // And the identity claims are still mapped exactly
    assertThat(jwtClaims).containsEntry("sub", USER_UUID.toString());
    assertThat(jwtClaims).containsEntry("auth_id", AUTH_ID);
    assertThat(jwtClaims).containsEntry("email", EMAIL);

    // And iat/exp keep the exact epoch-second values
    assertThat(jwtClaims).containsEntry("iat", ISSUED_AT.getEpochSecond());
    assertThat(jwtClaims).containsEntry("exp", EXPIRES_AT.getEpochSecond());
  }

  @Test
  void fromJwtClaims_whenAuthIdIsPresent_thenRebuildsUserAndKeepsTemporalArguments() {
    // Given
    final Map<String, Object> jwtClaims = Map.of(
        "sub", USER_UUID.toString(),
        "auth_id", AUTH_ID,
        "email", EMAIL,
        "name", NAME
    );

    // When
    final TokenClaims claims = JwtClaimsAdapter.fromJwtClaims(jwtClaims, ISSUED_AT, EXPIRES_AT);

    // Then - each identity field is reconstructed independently
    assertThat(claims.user().getId().getValue()).isEqualTo(USER_UUID);
    assertThat(claims.user().getAuthorizationId()).isEqualTo(AUTH_ID);
    assertThat(claims.user().getEmail()).isEqualTo(EMAIL);
    assertThat(claims.user().getName()).isEqualTo(NAME);

    // And the temporal values are exactly the supplied arguments
    assertThat(claims.issuedAt()).isEqualTo(ISSUED_AT);
    assertThat(claims.expiresAt()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void fromJwtClaims_whenAuthIdIsNull_thenFallsBackToUnknownAndPreservesOtherFields() {
    // Given - a legacy token without auth_id
    final Map<String, Object> jwtClaims = new HashMap<>();
    jwtClaims.put("sub", USER_UUID.toString());
    jwtClaims.put("auth_id", null);
    jwtClaims.put("email", EMAIL);
    jwtClaims.put("name", NAME);

    // When
    final TokenClaims claims = JwtClaimsAdapter.fromJwtClaims(jwtClaims, ISSUED_AT, EXPIRES_AT);

    // Then - the fallback authorization id is exactly "unknown"
    assertThat(claims.user().getAuthorizationId()).isEqualTo("unknown");

    // And the remaining identity fields are preserved
    assertThat(claims.user().getId().getValue()).isEqualTo(USER_UUID);
    assertThat(claims.user().getEmail()).isEqualTo(EMAIL);
    assertThat(claims.user().getName()).isEqualTo(NAME);

    // And the temporal values are exactly the supplied arguments
    assertThat(claims.issuedAt()).isEqualTo(ISSUED_AT);
    assertThat(claims.expiresAt()).isEqualTo(EXPIRES_AT);
  }

  @Test
  void fromJwtClaims_whenSubIsNotAValidUuid_thenPropagatesIllegalArgumentException() {
    // Given
    final Map<String, Object> jwtClaims = Map.of(
        "sub", "not-a-uuid",
        "auth_id", AUTH_ID,
        "email", EMAIL,
        "name", NAME
    );

    // Then - the UUID.fromString failure is propagated untouched
    assertThatThrownBy(() -> JwtClaimsAdapter.fromJwtClaims(jwtClaims, ISSUED_AT, EXPIRES_AT))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
