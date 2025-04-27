package com.myvanitys.api.product.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.ToString;

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

  public Product(EntityId id, String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
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

  public int getAverageRating() {
    return reviews.isEmpty() ? 0 : reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
  }

  public void addReview(Review review) {
    if (review == null) {
      throw new ProductValidationException("Review cannot be null");
    }
    if (!reviews.contains(review)) {
      reviews.add(review);
      calculateAverageRating();
    }
  }

  public void removeReview(Review review) {
    if (reviews.remove(review)) {
      calculateAverageRating();
    }
  }

  /**
   * Adds a new review to the product from a specific user This is the only way to create a new review
   *
   * @param userId The ID of the user adding the review
   * @param rating The rating (1-5)
   * @param comment The review comment
   * @return The created review
   * @throws ValidationException if the review cannot be added
   */
  public Review addReviewFromUser(EntityId userId, int rating, String comment) {
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
    Review review = Review.createFor(relation.getId(), rating, comment);

    // Update the relation with the review ID
    relation.setReviewId(review.getId());

    // Add to reviews collection
    reviews.add(review);

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
   * @throws ValidationException if the review does not exist or user does not own it
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

  /**
   * Removes a user's review
   *
   * @param userId The ID of the user who owns the review
   * @return true if the review was removed
   */
  public boolean removeReviewByUser(EntityId userId) {
    // Find the user's relation
    Optional<ProductUserRelation> relationOpt = userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst();

    if (relationOpt.isEmpty() || relationOpt.get().getReviewId() == null) {
      return false;
    }

    ProductUserRelation relation = relationOpt.get();
    EntityId reviewId = relation.getReviewId();

    // Find and remove the review
    Optional<Review> reviewOpt = findReviewById(reviewId);
    if (reviewOpt.isEmpty()) {
      return false;
    }

    // Remove the review
    boolean removed = reviews.remove(reviewOpt.get());

    if (removed) {
      // Clear the review ID from the relation
      relation.setReviewId(null);

      // Update average rating
      calculateAverageRating();
    }

    return removed;
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
   * Calculates and updates the average rating
   */
  public void calculateAverageRating() {
    averageRating = reviews.isEmpty() ? 0 :
        (int) Math.round(reviews.stream().mapToInt(Review::getRating).average().orElse(0));
  }

  /**
   * Finds or creates a relation between this product and a user
   *
   * @param userId The user ID
   * @return The existing or new relation
   */
  private ProductUserRelation findOrCreateUserRelation(EntityId userId) {
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
