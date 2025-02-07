package com.api.myvanitys.application.command;

import com.api.myvanitys.domain.model.Category;
import com.api.myvanitys.domain.valueobject.EntityId;

import java.util.Objects;

public record CreateProductCommand(EntityId id, String name, String brand, Category categoryID, String colorHex) {

  public void validate() {
    Objects.requireNonNull(id, "Product ID is required");
    validateString(name, "Product name is required");
    validateString(brand, "Brand is required");
    Objects.requireNonNull(categoryID, "Category is required");
    validateString(colorHex, "Color is required");
  }

  private void validateString(String value, String errorMessage) {
    Objects.requireNonNull(value, errorMessage);
    if (value.isEmpty()) {
      throw new IllegalArgumentException(errorMessage + " cannot be empty");
    }
  }
}

