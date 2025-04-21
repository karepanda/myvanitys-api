package com.myvanitys.api.product.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.stream.Stream;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CreateProductCommandTest {

  static EntityId validEntityId() {
    return new EntityId(UUID.randomUUID());
  }

  static Stream<Arguments> invalidInputs() {
    return Stream.of(
        Arguments.of(null, "BrandX", validEntityId(), "#FF5733", validEntityId(), "name", "Product name is required and cannot be empty"),
        Arguments.of("   ", "BrandX", validEntityId(), "#FF5733", validEntityId(), "name", "Product name is required and cannot be empty"),
        Arguments.of("Lipstick", null, validEntityId(), "#FF5733", validEntityId(), "brand", "Brand is required and cannot be empty"),
        Arguments.of("Lipstick", "   ", validEntityId(), "#FF5733", validEntityId(), "brand", "Brand is required and cannot be empty"),
        Arguments.of("Lipstick", "BrandX", null, "#FF5733", validEntityId(), "categoryId", "Category is required"),
        Arguments.of("Lipstick", "BrandX", validEntityId(), null, validEntityId(), "colorHex", "Color is required and cannot be empty"),
        Arguments.of("Lipstick", "BrandX", validEntityId(), " ", validEntityId(), "colorHex", "Color is required and cannot be empty"),
        Arguments.of("Lipstick", "BrandX", validEntityId(), "#FF5733", null, "userId", "User ID is required")
    );
  }

  @ParameterizedTest(name = "Should throw ValidationException when field {5} is invalid")
  @MethodSource("invalidInputs")
  void shouldThrowValidationExceptionWhenFieldIsInvalid(
      String name,
      String brand,
      EntityId categoryId,
      String colorHex,
      EntityId userId,
      String expectedField,
      String expectedMessage
  ) {
    var exception = assertThrows(ValidationException.class, () ->
        new CreateProductCommand(name, brand, categoryId, colorHex, userId)
    );

    // Get the errors list with explicit type
    var errors = exception.getErrors();
    assertThat(errors).hasSize(1);

    // Get the error and assert its properties
    var error = errors.getFirst();
    assertThat(error.field()).isEqualTo(expectedField);
    assertThat(error.message()).isEqualTo(expectedMessage);
  }

  @Test
  @DisplayName("Should create successfully when all fields are valid")
  void shouldCreateSuccessfully() {
    assertDoesNotThrow(() -> new CreateProductCommand(
        "Lipstick",
        "BrandX",
        validEntityId(),
        "#FF5733",
        validEntityId()
    ));
  }
}