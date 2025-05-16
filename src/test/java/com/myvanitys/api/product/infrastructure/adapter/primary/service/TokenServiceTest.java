package com.myvanitys.api.product.infrastructure.adapter.primary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.myvanitys.api.auth.domain.exception.TokenException;
import com.myvanitys.api.auth.domain.port.secondary.TokenGenerator;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock
  private TokenGenerator tokenGenerator;

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService(tokenGenerator);
  }

  @Test
  @DisplayName("Should extract user ID when token has Bearer prefix")
  void extractUserId_WithBearerToken_ShouldReturnUserId() {
    // Arrange
    String tokenWithBearer = "Bearer valid-jwt-token";
    String tokenWithoutBearer = "valid-jwt-token";
    UUID expectedUserId = UUID.fromString("01965972-7033-7950-9cb1-56fe1251e72e");

    when(tokenGenerator.extractUserId(tokenWithoutBearer)).thenReturn(expectedUserId);

    // Act
    UUID result = tokenService.extractUserId(tokenWithBearer);

    // Assert
    assertEquals(expectedUserId, result);
    verify(tokenGenerator).extractUserId(tokenWithoutBearer);
  }

  @Test
  @DisplayName("Should throw UnauthorizedException when token validation fails with TokenException")
  void extractUserId_WithInvalidToken_ShouldThrowUnauthorizedException() {
    // Arrange
    String invalidToken = "invalid-token";

    when(tokenGenerator.extractUserId(invalidToken))
        .thenThrow(new TokenException("Invalid token"));

    // Act & Assert
    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> tokenService.extractUserId(invalidToken));

    assertTrue(exception.getMessage().contains("Invalid token"));
    verify(tokenGenerator).extractUserId(invalidToken);
  }

  @Test
  @DisplayName("Should throw UnauthorizedException when token validation fails with other exceptions")
  void extractUserId_WithGenericError_ShouldThrowUnauthorizedException() {
    // Arrange
    String invalidToken = "error-token";

    when(tokenGenerator.extractUserId(invalidToken))
        .thenThrow(new RuntimeException("Unexpected error"));

    // Act & Assert
    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> tokenService.extractUserId(invalidToken));

    assertEquals("Authentication error", exception.getMessage());
    verify(tokenGenerator).extractUserId(invalidToken);
  }

  @Test
  @DisplayName("Should return true when token is valid")
  void isValidToken_WithValidToken_ShouldReturnTrue() {
    // Arrange
    String validToken = "valid-jwt-token";

    // Configurar el mock para que no lance excepción
    // No necesitamos configurar un retorno específico ya que el método es void

    // Act
    boolean result = tokenService.isValidToken(validToken);

    // Assert
    assertTrue(result);
    verify(tokenGenerator).validateToken(validToken);
  }

  @Test
  @DisplayName("Should return false when token is invalid")
  void isValidToken_WithInvalidToken_ShouldReturnFalse() {
    // Arrange
    String invalidToken = "invalid-token";

    doThrow(new TokenException("Invalid token"))
        .when(tokenGenerator).validateToken(invalidToken);

    // Act
    boolean result = tokenService.isValidToken(invalidToken);

    // Assert
    assertFalse(result);
    verify(tokenGenerator).validateToken(invalidToken);
  }
}