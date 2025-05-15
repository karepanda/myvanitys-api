package com.myvanitys.api.product.infrastructure.adapter.primary.service;

import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
    }

    @Test
    @DisplayName("Should extract user ID when token is valid")
    void extractUserId_WithValidToken_ShouldReturnUserId() {
        // Arrange
        String validToken = "4/P7q7W91";
        UUID expectedUserId = UUID.fromString("01965972-7033-7950-9cb1-56fe1251e72e");

        // Act
        UUID result = tokenService.extractUserId(validToken);

        // Assert
        assertEquals(expectedUserId, result);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when token is invalid")
    void extractUserId_WithInvalidToken_ShouldThrowUnauthorizedException() {
        // Arrange
        String invalidToken = "invalid-token";

        // Act & Assert
        assertThrows(UnauthorizedException.class, 
            () -> tokenService.extractUserId(invalidToken));
    }
}