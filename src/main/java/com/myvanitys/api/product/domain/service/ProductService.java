package com.myvanitys.api.product.domain.service;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Domain service for product and review related operations
 */
public interface ProductService {

  /**
   * Creates a new product associated with a user
   */
  Product createProduct(String name, String brand, Category category, String colorHex, EntityId userId);

  /**
   * Adds a review to a product
   */
  Review addReviewToProduct(EntityId productId, String comment, int rating, EntityId userId);

  /**
   * Updates an existing product
   */
  Product updateProduct(EntityId productId, String name, String brand, Category category, String colorHex);

  /**
   * Deletes a product
   */
  void deleteProduct(EntityId productId);

  /**
   * Finds a product by its ID
   */
  Optional<Product> getProductById(EntityId productId);

  /**
   * Gets all products owned by a user
   */
  List<Product> getProductsByUserId(EntityId userId);

  /**
   * Updates an existing review
   */
  Review updateReview(EntityId reviewId, String comment, int rating);

  /**
   * Deletes a review
   */
  void deleteReview(EntityId reviewId);

  /**
   * Gets all reviews for a product
   */
  List<Review> getReviewsByProductId(EntityId productId);

  /**
   * Checks if a user is the owner of a product
   */
  boolean isProductOwner(EntityId productId, EntityId userId);

  /**
   * Checks if a user is the owner of a review
   */
  boolean isReviewOwner(EntityId reviewId, EntityId userId);
}