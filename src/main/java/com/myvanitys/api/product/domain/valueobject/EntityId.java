package com.myvanitys.api.product.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class EntityId {

  private final UUID value;

  // Default constructor generating a new UUID
  public EntityId() {
    this.value = UUID.randomUUID();
  }

  public EntityId(UUID value) {
    this.value = Objects.requireNonNull(value, "Id value cannot be null");
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EntityId entityId)) {
      return false; // Matching pattern
    }
    return Objects.equals(value, entityId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}