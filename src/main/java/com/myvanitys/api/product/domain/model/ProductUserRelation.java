package com.myvanitys.api.product.domain.model;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;

@Getter
public class ProductUserRelation {

  private final EntityId id;

  private final EntityId productId;

  private final EntityId userId;

  // ---------- CONSTRUCTOR PRIVADO ----------
  private ProductUserRelation(EntityId id, EntityId productId, EntityId userId) {
    this.id = Objects.requireNonNull(id);
    this.productId = Objects.requireNonNull(productId);
    this.userId = Objects.requireNonNull(userId);
  }

  // ---------- FACTORY METHODS ----------

  /**
   * Creates a new ProductUserRelation (new entity)
   */
  public static ProductUserRelation create(EntityId productId, EntityId userId) {
    return new ProductUserRelation(EntityId.newId(), productId, userId);
  }

  /**
   * Reconstructs a relation from persistence
   */
  public static ProductUserRelation reconstruct(EntityId id, EntityId productId, EntityId userId) {
    return new ProductUserRelation(id, productId, userId);
  }

  // ---------- BEHAVIOR METHODS ----------

  public boolean belongsToUser(EntityId userId) {
    return this.userId.equals(userId);
  }

  public boolean belongsToProduct(EntityId productId) {
    return this.productId.equals(productId);
  }

  // ---------- IDENTITY ----------

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProductUserRelation that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ProductUserRelation[" + id + ", product=" + productId + ", user=" + userId + "]";
  }
}
