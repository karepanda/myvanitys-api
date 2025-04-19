package com.myvanitys.api.product.application.query;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Query to search for products associated to a user
 */
public record FindProductUserQuery(EntityId userId) {

  /**
   * Master builder with validation
   */
  public FindProductUserQuery {
    validate();
  }

  private void validate() {
    Objects.requireNonNull(userId, "User ID is required");
  }

  /**
   * Creates an instance of FindProductUserQuery
   *
   * @param userId user ID * @return a new instance of FindProductUserQuery
   */
  public static FindProductUserQuery of(EntityId userId) {
    return new FindProductUserQuery(userId);
  }
}