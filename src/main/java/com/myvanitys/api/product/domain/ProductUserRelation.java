package com.myvanitys.api.product.domain;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class ProductUserRelation {

  private final EntityId id;

  private final EntityId productId;

  private final EntityId userId;

  @Setter
  private EntityId reviewId;

  public ProductUserRelation(@NonNull EntityId id, @NonNull EntityId productId, @NonNull EntityId userId) {
    this.id = id;
    this.productId = productId;
    this.userId = userId;
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
    ProductUserRelation that = (ProductUserRelation) o;
    return Objects.equals(userId, that.userId) &&
        Objects.equals(productId, that.productId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, productId);
  }
}