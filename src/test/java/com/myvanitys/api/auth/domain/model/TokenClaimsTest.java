package com.myvanitys.api.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenClaimsTest {

  @Test
  @DisplayName("Should create valid TokenClaims")
  void shouldCreateValidTokenClaims() {
    // Arrange
    User user = createTestUser();
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);

    // Act
    TokenClaims claims = new TokenClaims(user, issuedAt, expiresAt);

    // Assert
    assertThat(claims.user()).isEqualTo(user);
    assertThat(claims.issuedAt()).isEqualTo(issuedAt);
    assertThat(claims.expiresAt()).isEqualTo(expiresAt);
  }

  @Test
  @DisplayName("Should throw TokenException when user is null")
  void shouldThrowExceptionWhenUserIsNull() {
    // Arrange
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);

    // Act & Assert
    assertThatThrownBy(() -> new TokenClaims(null, issuedAt, expiresAt))
        .isInstanceOf(TokenException.class)
        .hasMessageContaining("user cannot be null");
  }

  @Test
  @DisplayName("Should throw TokenException when issuedAt is null")
  void shouldThrowExceptionWhenIssuedAtIsNull() {
    // Arrange
    User user = createTestUser();
    Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

    // Act & Assert
    assertThatThrownBy(() -> new TokenClaims(user, null, expiresAt))
        .isInstanceOf(TokenException.class)
        .hasMessageContaining("issuedAt cannot be null");
  }

  @Test
  @DisplayName("Should throw TokenException when expiresAt is null")
  void shouldThrowExceptionWhenExpiresAtIsNull() {
    // Arrange
    User user = createTestUser();
    Instant issuedAt = Instant.now();

    // Act & Assert
    assertThatThrownBy(() -> new TokenClaims(user, issuedAt, null))
        .isInstanceOf(TokenException.class)
        .hasMessageContaining("expiresAt cannot be null");
  }

  @Test
  @DisplayName("Should throw TokenException when expiresAt is before issuedAt")
  void shouldThrowExceptionWhenExpiresAtIsBeforeIssuedAt() {
    // Arrange
    User user = createTestUser();
    Instant now = Instant.now();
    Instant expiresAt = now.minus(1, ChronoUnit.HOURS); // Expires in the past

    // Act & Assert
    assertThatThrownBy(() -> new TokenClaims(user, now, expiresAt))
        .isInstanceOf(TokenException.class)
        .hasMessageContaining("expiresAt cannot be before issuedAt");
  }

  @Test
  @DisplayName("Should create TokenClaims from User with factory method")
  void shouldCreateTokenClaimsFromUserWithFactory() {
    // Arrange
    User user = createTestUser();
    long expirationInSeconds = 3600L; // 1 hour
    Instant beforeCreation = Instant.now();

    // Act
    TokenClaims claims = TokenClaims.fromUser(user, expirationInSeconds);
    Instant afterCreation = Instant.now();

    // Assert
    assertThat(claims.user()).isEqualTo(user);

    // Check issuedAt is between beforeCreation and afterCreation
    assertThat(claims.issuedAt()).isAfterOrEqualTo(beforeCreation);
    assertThat(claims.issuedAt()).isBeforeOrEqualTo(afterCreation);

    // Check expiresAt is issuedAt + expirationInSeconds
    long actualExpirationSeconds = ChronoUnit.SECONDS.between(claims.issuedAt(), claims.expiresAt());
    assertThat(actualExpirationSeconds).isEqualTo(expirationInSeconds);
  }

  @Test
  @DisplayName("Should create TokenClaims with different expiration times")
  void shouldCreateTokenClaimsWithDifferentExpirations() {
    // Arrange
    User user = createTestUser();
    long[] expirations = {60L, 300L, 3600L, 86400L}; // 1 min, 5 min, 1 hour, 1 day

    for (long expiration : expirations) {
      // Act
      TokenClaims claims = TokenClaims.fromUser(user, expiration);

      // Assert
      long actualExpirationSeconds = ChronoUnit.SECONDS.between(claims.issuedAt(), claims.expiresAt());
      assertThat(actualExpirationSeconds).isEqualTo(expiration);
    }
  }

  private User createTestUser() {
    return new User(
        new EntityId(),
        "google-auth-id",
        "test@example.com",
        "Test User",
        Instant.now()
    );
  }
}