package com.myvanitys.api.product.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddProductToMyVanityCommandTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("when valid productId and userId are provided, then creates command successfully")
        void when_validProductIdAndUserId_then_createsCommandSuccessfully() {
            // Given
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            // When
            AddProductToMyVanityCommand command = new AddProductToMyVanityCommand(productId, userId);

            // Then
            assertThat(command.productId()).isEqualTo(productId);
            assertThat(command.userId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("when command is created, then toString contains productId and userId")
        void when_commandIsCreated_then_toStringContainsProductIdAndUserId() {
            // Given
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            AddProductToMyVanityCommand command = new AddProductToMyVanityCommand(productId, userId);

            // When
            String toString = command.toString();

            // Then
            assertThat(toString)
                    .contains("AddProductToMyVanityCommand")
                    .contains(productId.toString())
                    .contains(userId.toString());
        }
    }

    @Nested
    @DisplayName("Record Properties")
    class RecordPropertiesTests {

        @Test
        @DisplayName("when accessing productId property, then returns correct value")
        void when_accessingProductIdProperty_then_returnsCorrectValue() {
            // Given
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            AddProductToMyVanityCommand command = new AddProductToMyVanityCommand(productId, userId);

            // When & Then
            assertThat(command.productId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("when accessing userId property, then returns correct value")
        void when_accessingUserIdProperty_then_returnsCorrectValue() {
            // Given
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            AddProductToMyVanityCommand command = new AddProductToMyVanityCommand(productId, userId);

            // When & Then
            assertThat(command.userId()).isEqualTo(userId);
        }
    }
}