package com.myvanitys.api.product.domain.valueobject;

import java.time.Instant;

import com.myvanitys.api.product.domain.exception.ReviewValidationException;

/**
 * Value Object that encapsulates the details of a review including its entire temporal lifecycle
 */
public record ReviewDetails(int rating, String comment, Timestamp createdAt, Timestamp updatedAt, Timestamp deletedAt) {

  /**
   * Canonical constructor with validation
   */
  public ReviewDetails {
    validateRating(rating);
    validateComment(comment);
    // Timestamp already handles null values internally
  }

  /**
   * Factory method to create a new review
   */
  public static ReviewDetails create(int rating, String comment) {
    Timestamp now = Timestamp.now();
    return new ReviewDetails(rating, comment, now, now, null);
  }

  /**
   * Factory method to create review details with all timestamps
   */
  public static ReviewDetails of(int rating, String comment, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    return new ReviewDetails(
        rating,
        comment,
        Timestamp.of(createdAt),
        Timestamp.of(updatedAt),
        deletedAt != null ? Timestamp.of(deletedAt) : null
    );
  }

  /**
   * Creates a new version of this ReviewDetails with the rating and comment updated
   */
  public ReviewDetails withUpdates(int rating, String comment) {
    return new ReviewDetails(rating, comment, this.createdAt, Timestamp.now(), this.deletedAt);
  }

  /**
   * Marks this review as deleted
   */
  public ReviewDetails markAsDeleted() {
    if (isDeleted()) {
      return this; // Already deleted, do nothing
    }
    return new ReviewDetails(this.rating, this.comment, this.createdAt, this.updatedAt, Timestamp.now());
  }

  /**
   * Checks if this review has been deleted
   */
  public boolean isDeleted() {
    return deletedAt != null;
  }

  private static void validateRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw new ReviewValidationException("Rating must be between 1 and 5");
    }
  }

  private static void validateComment(String comment) {
    if (comment == null || comment.isBlank()) {
      throw new ReviewValidationException("Comment cannot be empty");
    }
  }
}
