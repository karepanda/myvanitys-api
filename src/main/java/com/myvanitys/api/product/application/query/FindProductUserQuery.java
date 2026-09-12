package com.myvanitys.api.product.application.query;

import com.myvanitys.api.common.valueobject.EntityId;

import java.util.Objects;


public record FindProductUserQuery(EntityId userId) {

  public FindProductUserQuery {
    Objects.requireNonNull(userId, "UserId must not be null");
  }
}