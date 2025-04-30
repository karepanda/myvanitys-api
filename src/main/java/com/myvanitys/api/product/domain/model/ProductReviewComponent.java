package com.myvanitys.api.product.domain.model;

import java.time.Instant;
import java.util.Optional;

import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;

/**
 * Component responsible for managing product reviews Uses sealed class to maintain a controlled relationship with the Product aggregate
 */
public final class ProductReviewComponent implements ProductOperations {

  public static final String USER_HAS_NO_RELATION_WITH_THIS_PRODUCT = "User has no relation with this product";

  public static final String REVIEW_NOT_FOUND = "Review not found";

  private final Product product;

  /**
   * Constructor only accessible by Product
   *
   * @param product The Product this component belongs to
   */
  ProductReviewComponent(Product product) {
    this.product = product;
  }

  /**
   * Gets the product this component operates on
   */
  @Override
  public Product getProduct() {
    return product;
  }

  /**
   * Adds a new review to the product from a specific user
   *
   * @param userId The ID of the user adding the review
   * @param details The details of the review
   * @return The created review
   */
  public Review addReviewFromUser(EntityId userId, ReviewDetails details) {
    // Find or create the product-user relation
    ProductUserRelation relation = product.findOrCreateUserRelation(userId);

    // Check if user already has a review
    if (relation.getReviewId() != null) {
      Optional<Review> existingReview = product.findReviewById(relation.getReviewId());
      if (existingReview.isPresent() && !existingReview.get().isDeleted()) {
        throw new ReviewValidationException("User already has a review for this product");
      }
    }

    // Create the review
    Review review = Review.createFor(relation.getId(), details);

    // Update the relation with the review ID
    relation.setReviewId(review.getId());

    // Add to reviews collection
    product.addReviewToCollection(review);

    // Update average rating
    product.calculateAverageRating();

    return review;
  }

  /**
   * Adds a new review to the product from a specific user
   *
   * @param userId The ID of the user adding the review
   * @param rating The rating (1-5)
   * @param comment The review comment
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, int rating, String comment) {
    return addReviewFromUser(userId, ReviewDetails.create(rating, comment));
  }

  /**
   * Adds a new review to the product from a specific user with specific timestamps
   *
   * @param userId The ID of the user adding the review
   * @param rating The rating (1-5)
   * @param comment The review comment
   * @param createdAt When the review was created
   * @param updatedAt When the review was last updated
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, int rating, String comment, Instant createdAt, Instant updatedAt) {
    return addReviewFromUser(userId,
        ReviewDetails.of(rating, comment, createdAt, updatedAt, null));
  }

  /**
   * Updates an existing review
   *
   * @param userId The ID of the user who owns the review
   * @param details The new review details
   * @return The updated review
   * @throws ReviewValidationException if the review does not exist or user does not own it
   */
  public Review updateReview(EntityId userId, ReviewDetails details) {
    // Find the user's relation
    ProductUserRelation relation = product.getUserRelations().stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new ReviewValidationException(USER_HAS_NO_RELATION_WITH_THIS_PRODUCT));

    // Check if user has a review
    if (relation.getReviewId() == null) {
      throw new ReviewValidationException(USER_HAS_NO_RELATION_WITH_THIS_PRODUCT);
    }

    // Find the review
    Review review = product.findReviewById(relation.getReviewId())
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    // Check if review is deleted
    if (review.isDeleted()) {
      throw new ReviewValidationException("Cannot update a deleted review");
    }

    // Update review
    review.updateDetails(details);

    // Update average rating
    product.calculateAverageRating();

    return review;
  }

  /**
   * Updates an existing review
   *
   * @param userId The ID of the user who owns the review
   * @param rating The new rating
   * @param comment The new comment
   * @return The updated review
   * @throws ReviewValidationException if the review does not exist or user does not own it
   */
  public Review updateReview(EntityId userId, int rating, String comment) {
    // Find the user's relation
    ProductUserRelation relation = product.getUserRelations().stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new ReviewValidationException(USER_HAS_NO_RELATION_WITH_THIS_PRODUCT));

    // Check if user has a review
    if (relation.getReviewId() == null) {
      throw new ReviewValidationException("User has no review for this product");
    }

    // Find the review
    Review review = product.findReviewById(relation.getReviewId())
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    // Check if review is deleted
    if (review.isDeleted()) {
      throw new ReviewValidationException("Cannot update a deleted review");
    }

    // Update review
    review.updateDetails(rating, comment);

    // Update average rating
    product.calculateAverageRating();

    return review;
  }

  /**
   * Marks a user's review as deleted (soft delete)
   *
   * @param userId The ID of the user who owns the review
   * @return The deleted review
   * @throws ReviewValidationException if the review does not exist or user does not own it
   */
  public Review deleteReview(EntityId userId) {
    // Find the user's relation
    ProductUserRelation relation = product.getUserRelations().stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new ReviewValidationException(USER_HAS_NO_RELATION_WITH_THIS_PRODUCT));

    // Check if user has a review
    if (relation.getReviewId() == null) {
      throw new ReviewValidationException(USER_HAS_NO_RELATION_WITH_THIS_PRODUCT);
    }

    // Find the review
    Review review = product.findReviewById(relation.getReviewId())
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    // Check if review is already deleted
    if (review.isDeleted()) {
      return review; // Already deleted, nothing to do
    }

    // Mark review as deleted
    review.markAsDeleted();

    // Update average rating (excluding deleted reviews)
    product.calculateAverageRating();

    return review;
  }

  /**
   * Physically removes a user's review from the product (hard delete)
   *
   * @param userId The ID of the user who owns the review
   * @return true if a review was removed
   */
  public boolean removeReview(EntityId userId) {
    // Find the user's relation
    Optional<ProductUserRelation> relationOpt = product.getUserRelations().stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst();

    if (relationOpt.isEmpty() || relationOpt.get().getReviewId() == null) {
      return false;
    }

    ProductUserRelation relation = relationOpt.get();
    EntityId reviewId = relation.getReviewId();

    // Find the review
    Optional<Review> reviewOpt = product.findReviewById(reviewId);
    if (reviewOpt.isEmpty()) {
      return false;
    }

    // Remove the review
    boolean removed = product.removeReviewFromCollection(reviewOpt.get());

    if (removed) {
      // Clear the review ID from the relation
      relation.setReviewId(null);

      // Update average rating
      product.calculateAverageRating();
    }

    return removed;
  }
}
