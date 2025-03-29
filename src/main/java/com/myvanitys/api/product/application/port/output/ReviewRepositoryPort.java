package com.myvanitys.api.product.application.port.output;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Port for review repository operations
 */
public interface ReviewRepositoryPort {

  /**
   * Saves a review
   *
   * @param review The review to save
   * @return The saved review with generated ID if new
   */
  Review save(Review review);

  /**
   * Finds a review by its ID
   *
   * @param reviewId Review ID
   * @return The review if exists
   */
  Optional<Review> findById(EntityId reviewId);

  /**
   * Finds reviews by product ID
   *
   * @param productId Product ID
   * @return List of reviews for the product
   */
  List<Review> findByProductId(EntityId productId);

  /**
   * Finds reviews by user ID
   *
   * @param userId User ID
   * @return List of reviews from the user
   */
  List<Review> findByUserId(EntityId userId);

  /**
   * Deletes a review by its ID
   *
   * @param reviewId Review ID
   */
  void deleteById(EntityId reviewId);
}