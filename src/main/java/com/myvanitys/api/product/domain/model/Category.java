package com.myvanitys.api.product.domain.model;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.exception.CategoryValidationException;

public record Category(EntityId categoryId, String name, CategoryCode code) {

  public Category {
    if (code == null) {
      throw new CategoryValidationException("Category code cannot be null");
    }
  }

  @Override
  public String toString() {
    return "Category{" +
        "id=" + categoryId +
        ", name='" + name + '\'' +
        ", code='" + code + '\'' +
        '}';
  }
}
