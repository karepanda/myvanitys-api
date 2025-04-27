package com.myvanitys.api.product.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * Value object representing a unique entity identifier
 */
@Getter
public final class EntityId {

  private final UUID value;

  /**
   * Creates a new EntityId with a random UUID
   */
  public EntityId() {
    this.value = UUID.randomUUID();
  }

  /**
   * Creates an EntityId with a specific UUID
   *
   * @param value The UUID value
   * @throws NullPointerException if value is null
   */
  public EntityId(UUID value) {
    this.value = Objects.requireNonNull(value, "Id value cannot be null");
  }

  /**
   * Factory method to create a new EntityId with a random UUID
   *
   * @return A new EntityId instance
   */
  public static EntityId newId() {
    return new EntityId();
  }

  /**
   * Factory method to create an EntityId from an existing UUID
   *
   * @param uuid The UUID value
   * @return A new EntityId instance
   */
  public static EntityId of(UUID uuid) {
    return new EntityId(uuid);
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