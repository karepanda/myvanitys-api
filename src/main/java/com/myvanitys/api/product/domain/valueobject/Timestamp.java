package com.myvanitys.api.product.domain.valueobject;

import java.time.Instant;

/**
 * Value object to represent a moment in time. It automatically assigns the current time when null is passed.
 */
public record Timestamp(Instant value) {

  /**
   * Constructor with validation
   */
  public Timestamp {
    // If the value is null, use the current instant
    value = value != null ? value : Instant.now();
  }

  /**
   * Factory method to create a timestamp with the current moment
   *
   * @return A new timestamp with the current instant
   */
  public static Timestamp now() {
    return new Timestamp(Instant.now());
  }

  /**
   * Factory method to create a timestamp from an Instant, or use the current moment if null
   *
   * @param instant The instant to use, or null to use the current moment
   * @return A new timestamp
   */
  public static Timestamp of(Instant instant) {
    return new Timestamp(instant);
  }

  /**
   * Utility method to get the value as an Instant, facilitating interoperability with code that expects an Instant
   *
   * @return The value as an Instant
   */
  public Instant asInstant() {
    return value;
  }

  /**
   * Checks if this timestamp is before another
   *
   * @param other The other timestamp
   * @return true if this timestamp is earlier
   */
  public boolean isBefore(Timestamp other) {
    return value.isBefore(other.value);
  }

  /**
   * Checks if this timestamp is after another
   *
   * @param other The other timestamp
   * @return true if this timestamp is later
   */
  public boolean isAfter(Timestamp other) {
    return value.isAfter(other.value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
