package com.myvanitys.api.product.domain.model;

import java.util.Objects;
import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Review {

  private final EntityId id;

  private final EntityId userId;

  private final Product product;

  private int rating;

  private String comment;

  /**
   * Creates a new review with validation
   */
  public Review(EntityId id, @NonNull EntityId userId, @NonNull Product product, int rating, @NonNull String comment) {
    validateRating(rating);

    this.id = id;
    this.userId = userId;
    this.product = product;
    this.rating = rating;
    this.comment = comment;
  }

  /**
   * Factory method to create a new review
   *
   * @param userId The user ID
   * @param product The product
   * @param rating The rating
   * @param comment The comment
   * @return A new Review instance
   */
  public static Review create(
      @NonNull EntityId userId,
      @NonNull Product product,
      int rating,
      @NonNull String comment) {

    return new Review(
        new EntityId(UUID.randomUUID()),
        userId,
        product,
        rating,
        comment
    );
  }

  /**
   * Factory method to create a review from a product-user relation
   *
   * @param id The review ID
   * @param productUserRelation The existing product-user relation
   * @param rating The rating
   * @param comment The comment
   * @param product The product associated with the review
   * @return A new Review instance
   */
  public static Review createFromRelation(
      EntityId id,
      @NonNull ProductUserRelation productUserRelation,
      int rating,
      @NonNull String comment,
      @NonNull Product product) {

    return new Review(
        id,
        productUserRelation.getUserId(),
        product,
        rating,
        comment
    );
  }

  /**
   * Updates review details with validation
   */
  public void updateDetails(int rating, @NonNull String comment) {
    validateRating(rating);

    this.rating = rating;
    this.comment = comment;
  }

  /**
   * Validates that the rating is between 1 and 5
   */
  private void validateRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Review review = (Review) o;
    return Objects.equals(id, review.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}