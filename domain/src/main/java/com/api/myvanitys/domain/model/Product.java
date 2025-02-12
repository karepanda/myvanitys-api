package com.api.myvanitys.domain.model;

import java.util.Objects;

import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Product {

  private final EntityId id;
  private final String name;
  private final String brand;
  private final Category category;
  private final String colorHex;

  public Product(EntityId id, @NonNull String name, @NonNull String brand, @NonNull Category category, @NonNull String colorHex) {

    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
