package com.api.myvanitys.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record EntityId(UUID value) {

  public EntityId() {
    this(UUID.randomUUID());
  }

  // Constructor que valida el valor
  public EntityId {
    Objects.requireNonNull(value, "Id value cannot be null");
  }

  @Override
  public String toString() {
    return value.toString();
  }
}