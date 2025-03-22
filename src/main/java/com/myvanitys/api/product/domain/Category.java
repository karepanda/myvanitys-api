package com.myvanitys.api.product.domain;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Category {

  private final EntityId id;

  private final String name;

  public Category(@NonNull EntityId id, @NonNull String name) {
    if (id == null) {
      throw new NullPointerException("id is marked non-null but is null");
    }
    if (name == null) {
      throw new NullPointerException("name is marked non-null but is null");
    }
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return "Category{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
