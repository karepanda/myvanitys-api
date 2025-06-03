package com.myvanitys.api.product.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.domain.valueobject.Timestamp;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;


@Getter
@ToString
public class Review {

  private final EntityId id;

  private final EntityId productUserId;

  private ReviewDetails details;

  private Review(EntityId id, EntityId productUserId, ReviewDetails details) {
    this.id = Objects.requireNonNull(id);
    this.productUserId = Objects.requireNonNull(productUserId);
    this.details = Objects.requireNonNull(details);
  }


  /**
   * Factory method for creating a new review with provided details Should only be used by the Product aggregate
   */
  static Review createFor(@NonNull EntityId productUserId, ReviewDetails details) {
    return new Review(EntityId.newId(), productUserId, details);
  }

  /**
   * Factory method for creating a new review with specific parameters Should only be used by the Product aggregate
   */
  static Review createFor(@NonNull EntityId productUserId, int rating, @NonNull String comment) {
    return createFor(productUserId, ReviewDetails.create(rating, comment));
  }

  /**
   * Factory method for creating a new review with all timestamps explicitly defined Should only be used by the Product aggregate
   */
  static Review createFor(@NonNull EntityId productUserId, int rating, @NonNull String comment,
      Instant createdAt, Instant updatedAt) {
    return createFor(productUserId,
        ReviewDetails.of(rating, comment, createdAt, updatedAt, null));
  }



  /**
   * Factory method for creating a review with an existing ID and details Should only be used by infrastructure mappers when reconstructing
   * from the database
   */
  public static Review createWithExistingId(EntityId id, @NonNull EntityId productUserId, ReviewDetails details) {
    return new Review(id, productUserId, details);
  }

  /**
   * Factory method for creating a review with an existing ID and all timestamps Should only be used by infrastructure mappers when
   * reconstructing from the database
   */
  public static Review createWithExistingId(EntityId id, @NonNull EntityId productUserId,
      int rating, @NonNull String comment, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    return createWithExistingId(id, productUserId,
        ReviewDetails.of(rating, comment, createdAt, updatedAt, deletedAt));
  }


  /**
   * Updates review details, preserving existing timestamps except for updatedAt
   */
  public void updateDetails(int rating, @NonNull String comment) {
    this.details = this.details.withUpdates(rating, comment);
  }

  /**
   * Updates review details with a complete new set of details Use with caution, as it will override all timestamps
   */
  public void updateDetails(ReviewDetails newDetails) {
    this.details = newDetails;
  }


  /**
   * Marks this review as deleted with the current timestamp
   */
  public void markAsDeleted() {
    this.details = this.details.markAsDeleted();
  }

  /**
   * Marks this review as deleted with a specific timestamp
   */
  public void markAsDeleted(Instant deletedAt) {
    if (deletedAt == null || this.details.isDeleted()) {
      return; // No action if already deleted or null timestamp
    }

    this.details = new ReviewDetails(
        this.details.rating(),
        this.details.comment(),
        this.details.createdAt(),
        this.details.updatedAt(),
        Timestamp.of(deletedAt)
    );
  }

  /**
   * Checks if this review is deleted
   */
  public boolean isDeleted() {
    return this.details.isDeleted();
  }

  /**
   * Checks if this review is deleted
   */
  public boolean isActive() {
    return !this.details.isDeleted();
  }


  /**
   * Gets the rating
   */
  public int getRating() {
    return details.rating();
  }

  /**
   * Gets the comment
   */
  public String getComment() {
    return details.comment();
  }

  /**
   * Gets the creation date as Instant
   */
  public Instant getCreatedAt() {
    return details.createdAt().asInstant();
  }

  /**
   * Gets the last update date as Instant
   */
  public Instant getUpdatedAt() {
    return details.updatedAt().asInstant();
  }

  /**
   * Gets the deletion date as Instant, or null if not deleted
   */
  public Instant getDeletedAt() {
    return details.deletedAt() != null ? details.deletedAt().asInstant() : null;
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
    return Objects.hash(id);
  }
}