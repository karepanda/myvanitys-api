package com.api.myvanitys.application.command;

import java.util.Objects;

public record CreateProductCommand(String name, String description) {

  public void validate() {
    validateString(name, "Product name is required");
    validateString(description, "Product description is required");
  }

  private void validateString(String value, String errorMessage) {
    Objects.requireNonNull(value, errorMessage);
    if (value.isEmpty()) {
      throw new IllegalArgumentException(errorMessage + " cannot be empty");
    }
  }
}

