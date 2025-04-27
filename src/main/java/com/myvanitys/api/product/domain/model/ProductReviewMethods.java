package com.myvanitys.api.product.domain.model;

import java.time.Instant;
import java.util.Optional;

import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;

/**
 * Métodos relacionados con revisiones para la clase Product Estos métodos deben integrarse en la clase Product
 */
public class ProductReviewMethods {

  /**
   * Adds a new review to the product from a specific user This is the only way to create a new review
   *
   * @param userId The ID of the user adding the review
   * @param details The details of the review
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, ReviewDetails details) {
    // Find or create the product-user relation
    ProductUserRelation relation = findOrCreateUserRelation(userId);

    // Check if user already has a review
    if (relation.getReviewId() != null) {
      Optional<Review> existingReview = findReviewById(relation.getReviewId());
      if (existingReview.isPresent()) {
        throw new ReviewValidationException("User already has a review for this product");
      }
    }

    // Create the review
    Review review = Review.createFor(relation.getId(), details);

    // Update the relation with the review ID
    relation.setReviewId(review.getId());

    // Add to reviews collection
    reviews.add(review);

    // Update average rating
    calculateAverageRating();

    return review;
  }

  /**
   * Adds a new review to the product from a specific user
   *
   * @param userId The ID of the user adding the review
   * @param rating The rating (1-5)
   * @param comment The review comment
   * @param createdAt When the review was created
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, int rating, String comment, Instant createdAt) {
    return addReviewFromUser(userId, ReviewDetails.of(rating, comment, createdAt));
  }

  /**
   * Adds a new review to the product from a specific user with current timestamp
   *
   * @param userId The ID of the user adding the review
   * @param rating The rating (1-5)
   * @param comment The review comment
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, int rating, String comment) {
    return addReviewFromUser(userId, rating, comment, Instant.now());
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
    ProductUserRelation relation = userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new ReviewValidationException("User has no relation with this product"));

    // Check if user has a review
    if (relation.getReviewId() == null) {
      throw new ReviewValidationException("User has no review for this product");
    }

    // Find the review
    Review review = findReviewById(relation.getReviewId())
        .orElseThrow(() -> new ReviewValidationException("Review not found"));

    // Update review
    review.updateDetails(details);

    // Update average rating
    calculateAverageRating();

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
    ProductUserRelation relation = userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new ReviewValidationException("User has no relation with this product"));

    // Check if user has a review
    if (relation.getReviewId() == null) {
      throw new ReviewValidationException("User has no review for this product");
    }

    // Find the review
    Review review = findReviewById(relation.getReviewId())
        .orElseThrow(() -> new ReviewValidationException("Review not found"));

    // Update review
    review.updateDetails(rating, comment);

    // Update average rating
    calculateAverageRating();

    return review;
  }
}