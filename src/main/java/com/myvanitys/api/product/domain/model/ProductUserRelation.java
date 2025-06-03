package com.myvanitys.api.product.domain.model;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;


@Getter
public class ProductUserRelation {

  private final EntityId id;

  private final EntityId productId;

  private final EntityId userId;

  private EntityId reviewId;

  private ProductUserRelation(EntityId id, EntityId productId, EntityId userId) {
    this.id = Objects.requireNonNull(id, "Id cannot be null");
    this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
    this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
  }

  public static ProductUserRelation create(EntityId productId, EntityId userId) {
    return new ProductUserRelation(EntityId.newId(), productId, userId);
  }

  public static ProductUserRelation reconstruct(EntityId id, EntityId productId, EntityId userId, EntityId reviewId) {
    ProductUserRelation relation = new ProductUserRelation(id, productId, userId);
    relation.reviewId = reviewId;
    return relation;
  }

  public boolean hasReview() {
    return reviewId != null;
  }

  public void linkToReview(EntityId reviewId) {
    this.reviewId = Objects.requireNonNull(reviewId, "Review ID cannot be null");
  }

  public void unlinkReview() {
    this.reviewId = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductUserRelation relation = (ProductUserRelation) o;
    return Objects.equals(productId, relation.productId) &&
        Objects.equals(userId, relation.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, userId);
  }

  @Override
  public String toString() {
    return "ProductUserRelation{" +
        "id=" + id +
        ", productId=" + productId +
        ", userId=" + userId +
        ", reviewId=" + reviewId +
        '}';
  }
}
