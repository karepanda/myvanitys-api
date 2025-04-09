package com.myvanitys.api.auth.application.service;

import com.myvanitys.api.auth.application.port.primary.command.GoogleAuthCommand;
import com.myvanitys.api.auth.domain.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GoogleAuthenticationServiceTest {
    private final GoogleAuthenticationService service = new GoogleAuthenticationService();

    @Test
    @DisplayName("Should authenticate and return valid UserSession")
    void shouldAuthenticateAndReturnUserSession() {
        // Arrange
        GoogleAuthCommand command = new GoogleAuthCommand("valid-code", "http://localhost/callback");
        UUID requestId = UUID.randomUUID();
        UUID flowId = UUID.randomUUID();

        // Act
        UserSession session = service.authenticateWithGoogle(command, requestId, flowId);

        // Assert
        assertNotNull(session);
        assertNotNull(session.token());
        assertTrue(session.isActive());

        assertNotNull(session.user());
        assertNotNull(session.user().getId());
        assertTrue(session.user().getAuthorizationId().startsWith("google-"));
        assertEquals("user@example.com", session.user().getEmail());
        assertEquals("User test", session.user().getName());
    }
}