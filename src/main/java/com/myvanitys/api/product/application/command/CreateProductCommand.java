package com.myvanitys.api.product.application.command;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;

public record CreateProductCommand(
    EntityId id,
    String name,
    String brand,
    EntityId categoryID,
    String colorHex,
    EntityId userId,
    String reviewText) {

  public void validate() {
    Objects.requireNonNull(id, "Product ID is required");
    validateString(name, "Product name is required");
    validateString(brand, "Brand is required");
    Objects.requireNonNull(categoryID, "Category is required");
    validateString(colorHex, "Color is required");
    Objects.requireNonNull(userId, "User ID is required");
    // No validamos reviewText porque es opcional
  }

  private void validateString(String value, String errorMessage) {
    Objects.requireNonNull(value, errorMessage);
    if (value.isEmpty()) {
      throw new IllegalArgumentException(errorMessage + " cannot be empty");
    }
  }
}