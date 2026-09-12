package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.auth.domain.model.TokenClaims;
import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.common.valueobject.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real, unmocked jjwt/Jackson serialization path (jjwt-jackson + BOM-managed Jackson 2).
 * Nothing jjwt-related is mocked here on purpose - this is the guardrail that fails if Jackson 2 ever
 * disappears from the runtime classpath.
 */
class JwtTokenGeneratorAdapterTest {

  private static final String SECRET = "myvanitys-test-secret-key-that-is-at-least-32-bytes-long!!";
  private static final long EXPIRATION_SECONDS = 3600L;
  private static final String ISSUER = "myvanitys";

  private final JwtTokenGeneratorAdapter adapter =
      new JwtTokenGeneratorAdapter(SECRET, EXPIRATION_SECONDS, ISSUER);

  private User sampleUser() {
    return new User(new EntityId(UUID.randomUUID()), "google-user-123", "user@example.com", "Test User");
  }

  @Test
  void generateToken_then_validateAndExtract_roundTripsTheSameUser() {
    // Given
    final User user = sampleUser();

    // When
    final String token = adapter.generateToken(adapter.createClaimsFromUser(user));

    // Then - a well-formed JWT is three base64url segments separated by dots
    assertThat(token.split("\\.")).hasSize(3);

    // And the real round-trip reconstructs the same user id
    final TokenClaims validated = adapter.validateToken(token);
    assertThat(validated).isNotNull();
    assertThat(validated.user().getId().getValue()).isEqualTo(user.getId().getValue());
    assertThat(adapter.extractUserId(token)).isEqualTo(user.getId().getValue());
  }

  @Test
  void validateToken_whenSignatureTampered_then_throwsTokenException() {
    // Given
    final User user = sampleUser();
    final String token = adapter.generateToken(adapter.createClaimsFromUser(user));
    final String tampered = tamperSignature(token);

    // Then - the real signature verification rejects the tampered token
    assertThatThrownBy(() -> adapter.validateToken(tampered)).isInstanceOf(TokenException.class);
    assertThatThrownBy(() -> adapter.extractUserId(tampered)).isInstanceOf(TokenException.class);
  }

  private String tamperSignature(String token) {
    final String signature = token.substring(token.lastIndexOf('.') + 1);
    final char replacement = signature.endsWith("A") ? 'B' : 'A';
    return token.substring(0, token.length() - 1) + replacement;
  }
}
