package com.myvanitys.api.auth.application.port.primary.command;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserCommandTest {

    @Nested
    class ConstructorTests{

        @Test
        void shouldCreateCommandSuccessfullyWithAllValidParameters() {
            // Arrange
            // Arrange
            String provider = "google";
            String code = "auth-code";
            String redirectUri = "http://example.com/callback";
            Instant registrationDate = Instant.now();

            // Act
            RegisterUserCommand command = new RegisterUserCommand(provider, code, redirectUri, registrationDate);

            // Assert
            assertAll(
                    () -> assertEquals(provider, command.provider()),
                    () -> assertEquals(code, command.code()),
                    () -> assertEquals(redirectUri, command.redirectUri()),
                    () -> assertEquals(registrationDate, command.registrationDate())
            );
        }

        @Test
        void shouldCreateCommandSuccessfullyWithMandatoryParameters() {
            // Arrange
            String provider = "google";
            String code = "auth-code";

            // Act
            RegisterUserCommand command = new RegisterUserCommand(provider, code, null, null);

            // Assert
            assertAll(
                    () -> assertEquals(provider, command.provider()),
                    () -> assertEquals(code, command.code()),
                    () -> assertNull(command.redirectUri()),
                    () -> assertNull(command.registrationDate())
            );
        }


    }



}