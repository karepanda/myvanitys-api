package com.myvanitys.api.product.domain;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Data;
import lombok.NonNull;

@Data
public class Review {

  private final EntityId id;

  private final EntityId userId;

  private final Product product;

  private final Integer rating;

  private final String description;

  public Review(EntityId id, @NonNull EntityId userId, @NonNull Product product, @NonNull Integer rating, @NonNull String review) {

    this.id = id;
    this.userId = userId;
    this.product = product;
    this.rating = rating;
    this.description = review;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, product, rating, description);
  }

  @Override
  public boolean equals(Object object) {
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
    Review review = (Review) object;
    return Objects.equals(id, review.id) && Objects.equals(userId, review.userId) && Objects.equals(product, review.product)
        && Objects.equals(rating, review.rating) && Objects.equals(description, review.description);
  }

  @Override
  public String toString() {
    return "Review{" +
        "id=" + id +
        ", user=" + userId +
        ", product=" + product +
        ", rating=" + rating +
        ", description='" + description + '\'' +
        '}';
  }

}
