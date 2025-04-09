package com.myvanitys.api.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @Test
    @DisplayName("Should create UserSession when token and user are valid")
    void shouldCreateUserSessionWhenValid() {
        // Arrange
        String token = "valid-token";
        User user = new User(new com.myvanitys.api.product.domain.valueobject.EntityId(java.util.UUID.randomUUID()),
                "google-123", "user@example.com", "Test User");

        // Act
        UserSession session = new UserSession(token, user);

        // Assert
        assertNotNull(session);
        assertEquals(token, session.token());
        assertEquals(user, session.user());
    }

    @Test
    @DisplayName("Should throw exception when token is null")
    void shouldThrowWhenTokenIsNull() {
        // Arrange
        User user = new User(new com.myvanitys.api.product.domain.valueobject.EntityId(java.util.UUID.randomUUID()),
                "google-123", "user@example.com", "Test User");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new UserSession(null, user)
        );
        assertEquals("Token cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when token is blank")
    void shouldThrowWhenTokenIsBlank() {
        // Arrange
        User user = new User(new com.myvanitys.api.product.domain.valueobject.EntityId(java.util.UUID.randomUUID()),
                "google-123", "user@example.com", "Test User");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new UserSession(" ", user)
        );
        assertEquals("Token cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user is null")
    void shouldThrowWhenUserIsNull() {
        // Arrange
        String token = "valid-token";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new UserSession(token, null)
        );
        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should return true when session is active")
    void shouldReturnTrueWhenSessionIsActive() {
        // Arrange
        String token = "active-token";
        User user = new User(new com.myvanitys.api.product.domain.valueobject.EntityId(java.util.UUID.randomUUID()),
                "google-123", "user@example.com", "Test User");

        UserSession session = new UserSession(token, user);

        // Act
        boolean isActive = session.isActive();

        // Assert
        assertTrue(isActive);
    }

    @Test
    @DisplayName("Should create UserSession using factory method")
    void shouldCreateUsingFactoryMethod() {
        // Arrange
        String token = "factory-token";
        User user = new User(new com.myvanitys.api.product.domain.valueobject.EntityId(java.util.UUID.randomUUID()),
                "google-123", "user@example.com", "Test User");

        // Act
        UserSession session = UserSession.create(token, user);

        // Assert
        assertNotNull(session);
        assertEquals(token, session.token());
        assertEquals(user, session.user());
    }
}