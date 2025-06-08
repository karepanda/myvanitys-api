package com.myvanitys.api.product.domain.model;

import java.time.Instant;
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

@Getter
@ToString
public class Product {

  public static final String USER_HAS_NO_RELATION = "User has no relation with this product";

  public static final String REVIEW_NOT_FOUND = "Review not found";

  public static final String CANNOT_UPDATE_DELETED_REVIEW = "Cannot update a deleted review";

  private final EntityId id;

  private String name;

  private String brand;

  private Category category;

  private String colorHex;

  private int averageRating;

  private final List<Review> reviews = new ArrayList<>();

  private final Set<ProductUserRelation> userRelations = new HashSet<>();

  Product(EntityId id, String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.id = Objects.requireNonNull(id, "Product ID cannot be null");
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
  }

  public static Product create(String name, String brand, Category category, String colorHex) {
    Objects.requireNonNull(category, "Category cannot be null when creating a product");
    return new Product(EntityId.newId(), name, brand, category, colorHex);
  }

  public static Product newProduct(String name, String brand, String colorHex) {
    return new Product(EntityId.newId(), name, brand, null, colorHex);
  }

  public void assignCategory(Category category) {
    if (this.category != null) {
      throw new ProductValidationException("Product already has a category assigned");
    }
    this.category = Objects.requireNonNull(category, "Category cannot be null");
  }

  public static Product reconstruct(EntityId id, String name, String brand, Category category,
      String colorHex, List<Review> reviews, Set<ProductUserRelation> userRelations) {
    Objects.requireNonNull(category, "Category cannot be null for reconstructed products");
    Product product = new Product(id, name, brand, category, colorHex);

    if (reviews != null) {
      product.reviews.addAll(reviews);
    }

    if (userRelations != null) {
      product.userRelations.addAll(userRelations);
    }

    product.calculateAverageRating();
    return product;
  }

  public void updateDetails(String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  // ✅ CORREGIDO: Sin linkToReview, simplemente agregar review
  public Review addReviewFromUser(EntityId userId, ReviewDetails details) {
    ProductUserRelation relation = findOrCreateUserRelation(userId);

    Review review = Review.createFor(relation.getId(), details);
    addReview(review);
    calculateAverageRating();

    return review;
  }

  public Review addReviewFromUser(EntityId userId, int rating, String comment) {
    return addReviewFromUser(userId, ReviewDetails.create(rating, comment));
  }

  public Review addReviewFromUser(EntityId userId, int rating, String comment, Instant createdAt, Instant updatedAt) {
    return addReviewFromUser(userId, ReviewDetails.of(rating, comment, createdAt, updatedAt, null));
  }

  // ✅ CORREGIDO: Buscar review directamente por ID, no por relación
  public Review updateReview(EntityId reviewId, ReviewDetails details) {
    Review review = findReviewById(reviewId)
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    if (review.isDeleted()) {
      throw new ReviewValidationException(CANNOT_UPDATE_DELETED_REVIEW);
    }

    review.updateDetails(details);
    calculateAverageRating();

    return review;
  }

  public Review updateReview(EntityId reviewId, int rating, String comment) {
    Review review = findReviewById(reviewId)
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    if (review.isDeleted()) {
      throw new ReviewValidationException(CANNOT_UPDATE_DELETED_REVIEW);
    }

    review.updateDetails(rating, comment);
    calculateAverageRating();

    return review;
  }

  public Review deleteReview(EntityId reviewId) {
    Review review = findReviewById(reviewId)
        .orElseThrow(() -> new ReviewValidationException(REVIEW_NOT_FOUND));

    if (review.isDeleted()) {
      return review;
    }

    review.markAsDeleted();
    calculateAverageRating();

    return review;
  }

  public boolean removeReview(EntityId reviewId) {
    Optional<Review> reviewOpt = findReviewById(reviewId);
    if (reviewOpt.isEmpty()) {
      return false;
    }

    boolean removed = reviews.remove(reviewOpt.get());
    if (removed) {
      calculateAverageRating();
    }

    return removed;
  }

  public boolean hasReviewFrom(EntityId userId) {
    EntityId productUserId = findUserRelation(userId)
        .map(ProductUserRelation::getId)
        .orElse(null);

    if (productUserId == null) {
      return false;
    }

    return reviews.stream()
        .filter(review -> review.getProductUserId().equals(productUserId))
        .anyMatch(Review::isActive);
  }

  public List<Review> findReviewsByUser(EntityId userId) {
    EntityId productUserId = findUserRelation(userId)
        .map(ProductUserRelation::getId)
        .orElse(null);

    if (productUserId == null) {
      return Collections.emptyList();
    }

    return reviews.stream()
        .filter(review -> review.getProductUserId().equals(productUserId))
        .toList();
  }

  // ✅ CORREGIDO: Obtener reviews activas de un usuario
  public List<Review> findActiveReviewsByUser(EntityId userId) {
    return findReviewsByUser(userId).stream()
        .filter(Review::isActive)
        .toList();
  }

  public Optional<Review> findReviewById(EntityId reviewId) {
    return reviews.stream()
        .filter(r -> r.getId().equals(reviewId))
        .findFirst();
  }

  // ✅ NUEVO: Helper para encontrar relación sin crear
  private Optional<ProductUserRelation> findUserRelation(EntityId userId) {
    return userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst();
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

  private ProductUserRelation findOrCreateUserRelation(EntityId userId) {
    return userRelations.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElseGet(() -> {
          ProductUserRelation newRelation = ProductUserRelation.create(id, userId);
          userRelations.add(newRelation);
          return newRelation;
        });
  }

  private void addReview(Review review) {
    if (review == null) {
      throw new IllegalArgumentException("Review cannot be null");
    }
    if (!reviews.contains(review)) {
      reviews.add(review);
    }
  }

  void calculateAverageRating() {
    List<Review> activeReviews = reviews.stream()
        .filter(Review::isActive)
        .toList();

    averageRating = activeReviews.isEmpty() ? 0 :
        (int) Math.round(activeReviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0));
  }

  public List<Review> getReviews() {
    return Collections.unmodifiableList(reviews);
  }

  public Set<ProductUserRelation> getUserRelations() {
    return Collections.unmodifiableSet(userRelations);
  }

  public int getActiveReviewCount() {
    return (int) reviews.stream()
        .filter(Review::isActive)
        .count();
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