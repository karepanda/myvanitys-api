package com.myvanitys.api.product.application.command;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateProductCommandTest {

    @Test
    @DisplayName("Should validate successfully when all fields are valid")
    void shouldValidateSuccessfully() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        assertDoesNotThrow(command::validate);
    }

    @Test
    @DisplayName("Should throw exception when product ID is null")
    void shouldThrowWhenProductIdIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                null,
                "Lipstick",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("Product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowWhenNameIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                null,
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("Product name is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowWhenNameIsEmpty() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, command::validate);
        assertEquals("Product name is required cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when brand is null")
    void shouldThrowWhenBrandIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                null,
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("Brand is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when brand is empty")
    void shouldThrowWhenBrandIsEmpty() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, command::validate);
        assertEquals("Brand is required cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when category ID is null")
    void shouldThrowWhenCategoryIdIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "BrandX",
                null,
                "#FF5733",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("Category is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when colorHex is null")
    void shouldThrowWhenColorHexIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                null,
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("Color is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when colorHex is empty")
    void shouldThrowWhenColorHexIsEmpty() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "",
                new EntityId(UUID.randomUUID()),
                "Great product!"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, command::validate);
        assertEquals("Color is required cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowWhenUserIdIsNull() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                new EntityId(UUID.randomUUID()),
                "Lipstick",
                "BrandX",
                new EntityId(UUID.randomUUID()),
                "#FF5733",
                null,
                "Great product!"
        );

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, command::validate);
        assertEquals("User ID is required", exception.getMessage());
    }
}