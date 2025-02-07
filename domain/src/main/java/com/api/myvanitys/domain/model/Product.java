package com.api.myvanitys.domain.model;

import java.util.Objects;

import com.api.myvanitys.domain.valueobject.EntityId;

public class Product {

  private final EntityId id;
  private String name;
  private String brand;
  private Category category;
  private String colorHex;

  public Product(EntityId id, String name, String brand, Category category, String colorHex) {
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Product product = (Product) object;

        return Objects.equals(id, product.id);
    }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Product{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", brand='" + brand + '\'' +
            ", category=" + category +
            ", colorHex='" + colorHex + '\'' +
            '}';
  }
}
