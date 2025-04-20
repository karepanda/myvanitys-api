package com.myvanitys.api.product.application.command;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.common.ValidationException.ValidationError;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public record CreateProductCommand(
    String name,
    String brand,
    EntityId categoryId,
    String colorHex,
    EntityId userId
) {

  public CreateProductCommand {
    List<ValidationError> errors = Stream.of(
        requiredNotBlank(name, "name", "Product name is required and cannot be empty"),
        requiredNotBlank(brand, "brand", "Brand is required and cannot be empty"),
        required(categoryId, "categoryId", "Category is required"),
        requiredNotBlank(colorHex, "colorHex", "Color is required and cannot be empty"),
        required(userId, "userId", "User ID is required")
    ).flatMap(Optional::stream).toList();

    if (!errors.isEmpty()) {
      throw ValidationException.withErrors(errors);
    }
  }

  private static Optional<ValidationError> required(Object value, String field, String message) {
    return value == null ? Optional.of(new ValidationError(field, message)) : Optional.empty();
  }

  private static Optional<ValidationError> requiredNotBlank(String value, String field, String message) {
    return (value == null || value.isBlank())
        ? Optional.of(new ValidationError(field, message))
        : Optional.empty();
  }
}
