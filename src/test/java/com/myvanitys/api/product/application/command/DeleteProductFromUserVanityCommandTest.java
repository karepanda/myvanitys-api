package com.myvanitys.api.product.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeleteProductFromUserVanityCommandTest {

    @Test
    @DisplayName("Should create command successfully with valid product ID and user ID")
    void shouldCreateCommandSuccessfully() {
        // Given
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // When
        DeleteProductFromUserVanityCommand command = new DeleteProductFromUserVanityCommand(productId, userId);

        // Then
        assertNotNull(command);
        assertEquals(productId, command.productId());
        assertEquals(userId, command.userId());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw IllegalArgumentException when product ID is null")
    void shouldThrowExceptionWhenProductIdIsNull(UUID nullProductId) {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DeleteProductFromUserVanityCommand(nullProductId, userId)
        );

        assertEquals("Product ID is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw IllegalArgumentException when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull(UUID nullUserId) {
        // Given
        UUID productId = UUID.randomUUID();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DeleteProductFromUserVanityCommand(productId, nullUserId)
        );

        assertEquals("User ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when both IDs are null")
    void shouldThrowExceptionWhenBothIdsAreNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DeleteProductFromUserVanityCommand(null, null)
        );

        assertEquals("Product ID is required", exception.getMessage());
    }

}