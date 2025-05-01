package com.myvanitys.api.product.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;

@Getter
public class Product {

  private final EntityId id;

  private final String name;

  private final String category;

  private final List<ProductUserRelation> userRelations;

  // ---------- CONSTRUCTOR PRIVADO ----------
  private Product(EntityId id, String name, String category, List<ProductUserRelation> userRelations) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.category = Objects.requireNonNull(category);
    this.userRelations = userRelations != null ? userRelations : Collections.emptyList();
  }

  // ---------- FACTORY METHODS ----------

  /**
   * Creates a new Product (to be persisted later)
   */
  public static Product create(String name, String category) {
    return new Product(EntityId.newId(), name, category, Collections.emptyList());
  }

  /**
   * Reconstructs a Product from persistence (e.g., database)
   */
  public static Product reconstruct(EntityId id, String name, String category, List<ProductUserRelation> relations) {
    return new Product(id, name, category, relations);
  }

  // ---------- BEHAVIOR METHODS ----------

  public boolean hasRelationWithUser(EntityId userId) {
    return userRelations.stream().anyMatch(r -> r.belongsToUser(userId));
  }

  public Product addRelation(ProductUserRelation relation) {
    List<ProductUserRelation> newRelations = List.copyOf(this.userRelations);
    newRelations.add(relation);
    return new Product(this.id, this.name, this.category, newRelations);
  }

  // equals/hashCode based on id only (entity rule)

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Product product)) {
      return false;
    }
    return id.equals(product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Product[" + id + ", " + name + ", " + category + "]";
  }
}