package com.myvanitys.api.product.domain;

import com.myvanitys.api.product.domain.valueobject.EntityId;

public record Category(EntityId id, String name) {

  @Override
  public String toString() {
    return "Category{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
