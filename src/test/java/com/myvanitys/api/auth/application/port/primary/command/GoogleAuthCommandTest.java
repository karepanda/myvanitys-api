package com.myvanitys.api.auth.application.port.primary.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleAuthCommandTest {

    @Test
    @DisplayName("Should create GoogleAuthCommand when code and redirectUri are valid")
    void shouldCreateCommandWhenValid() {
        // Arrange
        String validCode = "valid-auth-code";
        String validRedirectUri = "https://example.com/callback";

        // Act
        GoogleAuthCommand command = new GoogleAuthCommand(validCode, validRedirectUri);

        // Assert
        assertNotNull(command);
        assertEquals(validCode, command.code());
        assertEquals(validRedirectUri, command.redirectUri());
    }

    @Test
    @DisplayName("Should throw exception when code is null")
    void shouldThrowWhenCodeIsNull() {
        // Arrange
        String redirectUri = "https://example.com/callback";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new GoogleAuthCommand(null, redirectUri)
        );
        assertEquals("Authorization code cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when code is blank")
    void shouldThrowWhenCodeIsBlank() {
        // Arrange
        String redirectUri = "https://example.com/callback";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new GoogleAuthCommand("  ", redirectUri)
        );
        assertEquals("Authorization code cannot be null or blank", exception.getMessage());
    }

}