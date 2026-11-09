package com.myvanitys.api.product.domain.model;

import com.myvanitys.api.common.valueobject.EntityId;

public record Category(EntityId categoryId, String name, String code) {

  @Override
  public String toString() {
    return "Category{" +
        "id=" + categoryId +
        ", name='" + name + '\'' +
        ", code='" + code + '\'' +
        '}';
  }
}
