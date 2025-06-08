package com.myvanitys.api.product.domain.service;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.springframework.stereotype.Service;

@Service
public class ProductReview {

  /**
   * Calculates the overall average rating across multiple products
   */
  public double calculateOverallAverageRating(List<Product> products) {
    long totalReviews = 0;
    int totalRatingSum = 0;

    for (Product product : products) {
      List<Review> activeReviews = product.getReviews().stream()
          .filter(Review::isActive)
          .toList();

      totalReviews += activeReviews.size();
      totalRatingSum += activeReviews.stream()
          .mapToInt(Review::getRating)
          .sum();
    }

    return totalReviews > 0 ? (double) totalRatingSum / totalReviews : 0;
  }

  /**
   * Finds the user ID associated with a review in a product
   */
  public EntityId findUserIdForReview(Product product, EntityId reviewId) {
    Optional<Review> reviewOpt = product.findReviewById(reviewId);
    if (reviewOpt.isEmpty()) {
      return null;
    }

    Review review = reviewOpt.get();
    EntityId productUserId = review.getProductUserId();

    return product.getUserRelations().stream()
        .filter(relation -> productUserId.equals(relation.getId()))
        .map(ProductUserRelation::getUserId)
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets all reviews for a product
   */
  public List<Review> getUserReviewsAcrossProducts(List<Product> products, EntityId userId) {
    return products.stream()
        .flatMap(product -> product.findReviewsByUser(userId).stream())
        .toList();
  }

  /**
   * Gets active reviews from a specific user across multiple products
   */
  public List<Review> getUserActiveReviewsAcrossProducts(List<Product> products, EntityId userId) {
    return products.stream()
        .flatMap(product -> product.findActiveReviewsByUser(userId).stream())
        .toList();
  }

  /**
   * Calculates user's average rating across multiple products
   */
  public double calculateUserAverageRating(List<Product> products, EntityId userId) {
    List<Review> userActiveReviews = getUserActiveReviewsAcrossProducts(products, userId);

    if (userActiveReviews.isEmpty()) {
      return 0;
    }

    return userActiveReviews.stream()
        .mapToInt(Review::getRating)
        .average()
        .orElse(0);
  }

  /**
   * Gets statistics for a product
   */
  public ProductReviewStats getProductStats(Product product) {
    List<Review> activeReviews = product.getReviews().stream()
        .filter(Review::isActive)
        .toList();

    int totalReviews = activeReviews.size();
    double averageRating = totalReviews > 0 ?
        activeReviews.stream().mapToInt(Review::getRating).average().orElse(0) : 0;

    return new ProductReviewStats(totalReviews, averageRating, product.getAverageRating());
  }

  /**
   * Stats class for product reviews
   */
  public static class ProductReviewStats {

    private final int totalReviews;

    private final double exactAverageRating;

    private final int roundedAverageRating;

    public ProductReviewStats(int totalReviews, double exactAverageRating, int roundedAverageRating) {
      this.totalReviews = totalReviews;
      this.exactAverageRating = exactAverageRating;
      this.roundedAverageRating = roundedAverageRating;
    }

    public int getTotalReviews() {
      return totalReviews;
    }

    public double getExactAverageRating() {
      return exactAverageRating;
    }

    public int getRoundedAverageRating() {
      return roundedAverageRating;
    }
  }
}