package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for review entities
 */
@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

  /**
   * Finds reviews by product user ID
   */
  List<ReviewEntity> findByProductUserEntityProductUserId(UUID productUserId);

  /**
   * Finds reviews by product ID through the product user relationship
   */
  List<ReviewEntity> findByProductUserEntityProductId(UUID productId);

  /**
   * Finds reviews by user ID through the product user relationship
   */
  List<ReviewEntity> findByProductUserEntityUserId(UUID userId);

  /**
   * Checks if a review exists with the given ID and is associated with a specific user
   */
  boolean existsByReviewIdAndProductUserEntityUserId(UUID reviewId, UUID userId);

  /**
   * Find reviews by rating
   */
  List<ReviewEntity> findByRating(int rating);

  /**
   * Find reviews by product ID and rating greater than or equal to value
   */
  List<ReviewEntity> findByProductUserEntityProductIdAndRatingGreaterThanEqual(UUID productId, int rating);
}