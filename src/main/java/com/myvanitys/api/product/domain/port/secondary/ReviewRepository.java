package com.myvanitys.api.product.domain.port.secondary;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;


public interface ReviewRepository {

  /**
   * Save a review
   *
   * @param review the review to save
   * @return the saved review
   */
  Review save(Review review);

  /**
   * Find a review by its ID
   *
   * @param reviewId the review ID
   * @return the review if found
   */
  Optional<Review> findById(EntityId reviewId);

  /**
   * Delete a review by its ID
   *
   * @param reviewId the review ID to delete
   */
  void deleteById(EntityId reviewId);

  /**
   * Find all reviews for a product
   *
   * @param productId the product ID
   * @return list of reviews for the product
   */
  List<Review> findByProductId(EntityId productId);

  /**
   * Find all reviews by a user
   *
   * @param userId the user ID
   * @return list of reviews by the user
   */
  List<Review> findByUserId(EntityId userId);

  List<Review> findByProductUserId(EntityId productUserId);

  /**
   * Check if a review belongs to a user
   *
   * @param reviewId the review ID
   * @param userId the user ID
   * @return true if the review belongs to the user
   */
  boolean existsByReviewIdAndUserId(EntityId reviewId, EntityId userId);

}