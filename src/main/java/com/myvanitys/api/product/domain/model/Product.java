package com.myvanitys.api.product.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import lombok.Getter;
import lombok.ToString;

/**
 * Product aggregate root that manages reviews and user relations
 */
@Getter
@ToString
public class Product {

  private final EntityId id;

  private String name;

  private String brand;

  private Category category;

  private String colorHex;

  private int averageRating;

  private final List<Review> reviews = new ArrayList<>();

  private final Set<ProductUserRelation> userRelations = new HashSet<>();

  private final ProductOperations reviewComponent;

  public Product(EntityId id, String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
    this.reviewComponent = new ProductReviewComponent(this);
  }

  public void updateDetails(String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  private void validateProductDetails(String name, String brand, String colorHex) {
    validateNotEmpty(name, "name");
    validateNotEmpty(brand, "brand");
    validateColorHex(colorHex);
  }

  private void validateNotEmpty(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new ProductValidationException(fieldName + " cannot be empty");
    }
  }

  private void validateColorHex(String colorHex) {
    if (colorHex != null && !colorHex.trim().isEmpty() &&
        !colorHex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
      throw new ProductValidationException("Invalid color hex format");
    }
  }

  // Review delegation methods - public API

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
    return ((ProductReviewComponent) reviewComponent).addReviewFromUser(userId, rating, comment);
  }

  /**
   * Adds a new review to the product from a specific user with specific details
   *
   * @param userId The ID of the user adding the review
   * @param details The details of the review
   * @return The created review
   * @throws ReviewValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, ReviewDetails details) {
    return ((ProductReviewComponent) reviewComponent).addReviewFromUser(userId, details);
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
    return ((ProductReviewComponent) reviewComponent).updateReview(userId, rating, comment);
  }

  /**
   * Marks a user's review as deleted (soft delete)
   *
   * @param userId The ID of the user who owns the review
   * @return The deleted review
   * @throws ReviewValidationException if the review does not exist or user does not own it
   */
  public Review deleteReview(EntityId userId) {
    return ((ProductReviewComponent) reviewComponent).deleteReview(userId);
  }

  /**
   * Physically removes a user's review (hard delete) This method should be used with caution as it permanently removes data
   *
   * @param userId The ID of the user who owns the review
   * @return true if the review was removed
   */
  public boolean removeReviewByUser(EntityId userId) {
    return ((ProductReviewComponent) reviewComponent).removeReview(userId);
  }

  // Methods for internal use by ProductReviewComponent

  /**
   * Adds a review to the collection and recalculates the average rating This method is package-private for use by ProductReviewComponent
   */
  void addReviewToCollection(Review review) {
    if (review == null) {
      throw new ProductValidationException("Review cannot be null");
    }
    if (!reviews.contains(review)) {
      reviews.add(review);
    }
  }

  /**
   * Removes a review from the collection This method is package-private for use by ProductReviewComponent
   *
   * @return true if the review was removed
   */
  boolean removeReviewFromCollection(Review review) {
    return reviews.remove(review);
  }

  /**
   * Finds or creates a relation between this product and a user This method is package-private for use by ProductReviewComponent
   *
   * @param userId The user ID
   * @return The existing or new relation
   */
  ProductUserRelation findOrCreateUserRelation(EntityId userId) {
    return userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseGet(() -> {
          ProductUserRelation newRelation = new ProductUserRelation(
              EntityId.newId(),
              id,
              userId,
              null
          );
          userRelations.add(newRelation);
          return newRelation;
        });
  }

  /**
   * Finds a review by its ID
   *
   * @param reviewId The review ID
   * @return The review, if found
   */
  public Optional<Review> findReviewById(EntityId reviewId) {
    return reviews.stream()
        .filter(r -> r.getId().equals(reviewId))
        .findFirst();
  }

  /**
   * Finds a user's review
   *
   * @param userId The user ID
   * @return The user's review, if any
   */
  public Optional<Review> findReviewByUser(EntityId userId) {
    return userRelations.stream()
        .filter(r -> r.getUserId().equals(userId) && r.getReviewId() != null)
        .findFirst()
        .flatMap(r -> findReviewById(r.getReviewId()));
  }

  /**
   * Calculates and updates the average rating considering only active (non-deleted) reviews
   */
  void calculateAverageRating() {
    List<Review> activeReviews = reviews.stream()
        .filter(review -> !review.isDeleted())
        .toList();

    averageRating = activeReviews.isEmpty() ? 0 :
        (int) Math.round(activeReviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0));
  }

  /**
   * Gets an unmodifiable view of all reviews
   *
   * @return List of reviews
   */
  public List<Review> getReviews() {
    return Collections.unmodifiableList(reviews);
  }

  /**
   * Gets an unmodifiable view of all user relations
   *
   * @return Set of user relations
   */
  public Set<ProductUserRelation> getUserRelations() {
    return Collections.unmodifiableSet(userRelations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}