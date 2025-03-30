package com.myvanitys.api.product.domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

  // Users who have this product in their vanity
  private final Set<ProductUserRelation> userRelations = new HashSet<>();

  /**
   * Creates a new product with validation
   */
  public Product(EntityId id, String name, String brand, Category category, String colorHex) {
    validateName(name);
    validateBrand(brand);
    validateColorHex(colorHex);

    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
  }

  /**
   * Updates product details with validation
   */
  public void updateDetails(String name, String brand, Category category, String colorHex) {
    validateName(name);
    validateBrand(brand);
    validateColorHex(colorHex);

    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  /**
   * Validates that the product name is not empty
   */
  private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Product name cannot be empty");
    }
  }

  /**
   * Validates that the brand name is not empty
   */
  private void validateBrand(String brand) {
    if (brand == null || brand.trim().isEmpty()) {
      throw new IllegalArgumentException("Brand name cannot be empty");
    }
  }

  /**
   * Validates the color hex format
   */
  private void validateColorHex(String colorHex) {
    if (colorHex != null && !colorHex.trim().isEmpty() &&
        !colorHex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
      throw new IllegalArgumentException("Invalid color hex format");
    }
  }

  public int getAverageRating() {
    if (reviews.isEmpty()) {
      return 0;
    }

    int totalRating = 0;
    for (Review review : reviews) {
      totalRating += review.getRating();
    }

    return totalRating / reviews.size();
  }

  public void addReview(Review review) {
    if (review == null) {
      throw new IllegalArgumentException("Review cannot be null");
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

  // Method to associate a product with a user (add it to their vanity)
  public void addToUserVanity(EntityId userId, String reviewText) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Create the product-user relationship
    ProductUserRelation relation = new ProductUserRelation(new EntityId(), this.id, userId);

    // If a review is provided, add it
    if (reviewText != null && !reviewText.trim().isEmpty()) {
      Review review = new Review(new EntityId(), userId, this, 5, reviewText);
      this.addReview(review);
      relation.setReviewId(review.getId());
    }

    // Add the relationship to the collection
    userRelations.add(relation);
  }

  // Method to remove a product from a user's vanity
  public void removeFromUserVanity(EntityId userId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    userRelations.removeIf(relation -> relation.getUserId().equals(userId));
  }

  private void calculateAverageRating() {
    averageRating = reviews.isEmpty() ? 0 :
        reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Product product = (Product) obj;
    return Objects.equals(id, product.id);
  }
}