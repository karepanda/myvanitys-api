package com.myvanitys.api.product.application.query;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Query to search for products associated to a user
 */
public record FindProductUserQuery(EntityId userId) {

  public FindProductUserQuery {
    Objects.requireNonNull(userId, "UserId must not be null");
  }
}