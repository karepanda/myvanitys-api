package com.api.myvanitys.domain.model;

import java.util.Objects;

import com.api.myvanitys.domain.valueobject.EntityId;

public class Product {

  private final EntityId id;

  private final String name;

  private final String description;

  public Product(EntityId id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description);
  }

  @Override
  public String toString() {
    return "Product{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}
