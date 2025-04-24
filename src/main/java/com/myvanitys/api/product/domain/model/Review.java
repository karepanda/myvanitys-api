package com.myvanitys.api.product.domain.model;

import java.util.Objects;
import java.util.UUID;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Review {

  private final EntityId id;

  private final EntityId userId;

  private final EntityId productUserEntity;

  private int rating;

  private String comment;

  public static Review create(@NonNull EntityId userId,
                              @NonNull EntityId productUserEntity,
                              int rating,
                              @NonNull String comment) {
    Review review = new Review(new EntityId(UUID.randomUUID()), userId, productUserEntity, rating, comment);
    review.validateRating(rating);
    review.validateComment(comment);
    return review;

  }


  /**
   * Updates review details with validation
   */
  public void updateDetails(int rating, @NonNull String comment) {
    validateRating(rating);
    validateComment(comment);

    this.rating = rating;
    this.comment = comment;
  }

  /**
   * Validates that the rating is between 1 and 5
   */
  private void validateRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw ValidationException.withError("rating", "Rating must be between 1 and 5");
    }
  }

  /**
   * Validates that the comment is not empty
   */
  private void validateComment(String comment) {
    if (comment == null || comment.isBlank()) {
      throw ValidationException.withError("comment", "Comment cannot be empty");
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