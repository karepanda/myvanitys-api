package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTest {

  private EntityId productId;

  private EntityId userId;

  private Category category;

  private Product product;

  @BeforeEach
  void setUp() {
    productId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

    product = new Product(productId, "Test Product", "Test Brand", category, "#FFFFFF");
  }

  @Test
  void shouldInitializeProductCorrectly() {
    assertThat(product.getId()).isEqualTo(productId);
    assertThat(product.getName()).isEqualTo("Test Product");
    assertThat(product.getBrand()).isEqualTo("Test Brand");
    assertThat(product.getCategory()).isEqualTo(category);
    assertThat(product.getColorHex()).isEqualTo("#FFFFFF");
    assertThat(product.getAverageRating()).isEqualTo(0);
    assertThat(product.getReviews()).isEmpty();
    assertThat(product.getUserRelations()).isEmpty();
  }

  @Test
  void shouldUpdateProductDetails() {
    // Given
    Category newCategory = new Category(new EntityId(UUID.randomUUID()), "New Category");

    // When
    product.updateDetails("Updated Product", "Updated Brand", newCategory, "#000000");

    // Then
    assertThat(product.getName()).isEqualTo("Updated Product");
    assertThat(product.getBrand()).isEqualTo("Updated Brand");
    assertThat(product.getCategory()).isEqualTo(newCategory);
    assertThat(product.getColorHex()).isEqualTo("#000000");
  }

  @Test
  void shouldThrowExceptionForInvalidDetails() {
    // Name cannot be empty
    assertThatThrownBy(() -> product.updateDetails("", "Brand", category, "#FFFFFF"))
        .isInstanceOf(ProductValidationException.class)
        .hasMessageContaining("name cannot be empty");

    // Brand cannot be empty
    assertThatThrownBy(() -> product.updateDetails("Name", "", category, "#FFFFFF"))
        .isInstanceOf(ProductValidationException.class)
        .hasMessageContaining("brand cannot be empty");

    // Color hex must be valid
    assertThatThrownBy(() -> product.updateDetails("Name", "Brand", category, "invalid-color"))
        .isInstanceOf(ProductValidationException.class)
        .hasMessageContaining("Invalid color hex format");
  }

  @Test
  void shouldThrowExceptionWhenAddingReviewWithoutUserRelation() {
    // When/Then - Should throw exception because user relation doesn't exist
    assertThatThrownBy(() -> product.addReviewFromUser(userId, 4, "Great product"))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("User must be associated with product before adding reviews");
  }

  @Test
  void shouldAddReviewFromUserWhenRelationExists() {
    // Given - Create product with existing user relation
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    // When
    Review review = productWithRelation.addReviewFromUser(userId, 4, "Great product");

    // Then
    assertThat(productWithRelation.getReviews()).hasSize(1);
    assertThat(productWithRelation.getReviews().get(0)).isEqualTo(review);
    assertThat(productWithRelation.getAverageRating()).isEqualTo(4);
    assertThat(productWithRelation.getUserRelations()).hasSize(1);
  }

  @Test
  void shouldAddMultipleReviewsFromSameUser() {
    // Given - Create product with existing user relation
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    // When - User can add multiple reviews
    Review review1 = productWithRelation.addReviewFromUser(userId, 4, "First review");
    Review review2 = productWithRelation.addReviewFromUser(userId, 5, "Second review");

    // Then
    assertThat(productWithRelation.getReviews()).hasSize(2);
    assertThat(productWithRelation.getReviews()).contains(review1, review2);
    assertThat(productWithRelation.getAverageRating()).isEqualTo(5); // (4+5)/2 = 4.5 rounded to 5

    // Only one user relation should exist
    assertThat(productWithRelation.getUserRelations()).hasSize(1);

    // User should have multiple reviews
    List<Review> userReviews = productWithRelation.findReviewsByUser(userId);
    assertThat(userReviews).hasSize(2);
    assertThat(userReviews).contains(review1, review2);
  }

  @Test
  void shouldUpdateExistingReview() {
    // Given - Create product with review
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review = productWithRelation.addReviewFromUser(userId, 3, "Initial review");
    EntityId reviewId = review.getId();

    // When - Update by review ID
    Review updatedReview = productWithRelation.updateReview(reviewId, 5, "Updated review");

    // Then
    assertThat(updatedReview).isEqualTo(review); // Same review object, just updated
    assertThat(updatedReview.getRating()).isEqualTo(5);
    assertThat(updatedReview.getComment()).isEqualTo("Updated review");
    assertThat(productWithRelation.getAverageRating()).isEqualTo(5);
  }

  @Test
  void shouldSoftDeleteReview() {
    // Given - Create product with review
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review = productWithRelation.addReviewFromUser(userId, 4, "Review to delete");
    assertThat(productWithRelation.getAverageRating()).isEqualTo(4);

    // When - Delete by review ID
    Review deletedReview = productWithRelation.deleteReview(review.getId());

    // Then
    assertThat(deletedReview).isEqualTo(review);
    assertThat(deletedReview.isDeleted()).isTrue();

    // The review is still in the collection but marked as deleted
    assertThat(productWithRelation.getReviews()).hasSize(1);

    // The average rating should exclude deleted reviews
    assertThat(productWithRelation.getAverageRating()).isEqualTo(0);
  }

  @Test
  void shouldHardDeleteReview() {
    // Given - Create product with review
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review = productWithRelation.addReviewFromUser(userId, 4, "Review to delete");
    assertThat(productWithRelation.getReviews()).hasSize(1);

    // When - Remove by review ID
    boolean removed = productWithRelation.removeReview(review.getId());

    // Then
    assertThat(removed).isTrue();
    assertThat(productWithRelation.getReviews()).isEmpty();
    assertThat(productWithRelation.getAverageRating()).isEqualTo(0);

    // The user relation should still exist
    assertThat(productWithRelation.getUserRelations()).hasSize(1);
  }

  @Test
  void shouldFindReviewsByUser() {
    // Given - Create product with reviews
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review1 = productWithRelation.addReviewFromUser(userId, 4, "First review");
    Review review2 = productWithRelation.addReviewFromUser(userId, 5, "Second review");

    // When
    List<Review> userReviews = productWithRelation.findReviewsByUser(userId);

    // Then
    assertThat(userReviews).hasSize(2);
    assertThat(userReviews).contains(review1, review2);
  }

  @Test
  void shouldFindActiveReviewsByUser() {
    // Given - Create product with reviews
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review1 = productWithRelation.addReviewFromUser(userId, 4, "First review");
    Review review2 = productWithRelation.addReviewFromUser(userId, 5, "Second review");

    // Delete one review
    productWithRelation.deleteReview(review1.getId());

    // When
    List<Review> activeReviews = productWithRelation.findActiveReviewsByUser(userId);

    // Then
    assertThat(activeReviews).hasSize(1);
    assertThat(activeReviews).contains(review2);
    assertThat(activeReviews).doesNotContain(review1);
  }

  @Test
  void shouldCalculateAverageRatingCorrectly() {
    // Given - Create product with multiple user relations
    EntityId user1 = new EntityId(UUID.randomUUID());
    EntityId user2 = new EntityId(UUID.randomUUID());
    EntityId user3 = new EntityId(UUID.randomUUID());

    ProductUserRelation relation1 = ProductUserRelation.create(productId, user1);
    ProductUserRelation relation2 = ProductUserRelation.create(productId, user2);
    ProductUserRelation relation3 = ProductUserRelation.create(productId, user3);

    Product productWithRelations = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(relation1, relation2, relation3));

    Review review1 = productWithRelations.addReviewFromUser(user1, 5, "Excellent");
    Review review2 = productWithRelations.addReviewFromUser(user2, 3, "Average");
    Review review3 = productWithRelations.addReviewFromUser(user3, 4, "Good");

    // When
    productWithRelations.calculateAverageRating();

    // Then
    assertThat(productWithRelations.getAverageRating()).isEqualTo(4); // (5+3+4)/3 = 4

    // Soft delete one review
    productWithRelations.deleteReview(review2.getId());

    // Recalculate
    productWithRelations.calculateAverageRating();

    // Average should now exclude the deleted review
    assertThat(productWithRelations.getAverageRating()).isEqualTo(5); // (5+4)/2 = 4.5 rounded to 5
  }

  @Test
  void shouldCheckIfUserHasReviews() {
    // Given - Initially no reviews and no relation
    assertThat(product.hasReviewFrom(userId)).isFalse();

    // Given - Create product with user relation
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    // Initially no reviews even with relation
    assertThat(productWithRelation.hasReviewFrom(userId)).isFalse();

    // When - Add a review
    productWithRelation.addReviewFromUser(userId, 4, "Test review");

    // Then
    assertThat(productWithRelation.hasReviewFrom(userId)).isTrue();

    // When - Add another review (same user)
    productWithRelation.addReviewFromUser(userId, 5, "Another review");

    // Then - Still true
    assertThat(productWithRelation.hasReviewFrom(userId)).isTrue();
  }

  @Test
  void shouldHandleDeletedReviewsInHasReviewFrom() {
    // Given - Create product with user relation and reviews
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review1 = productWithRelation.addReviewFromUser(userId, 4, "First review");
    Review review2 = productWithRelation.addReviewFromUser(userId, 5, "Second review");

    assertThat(productWithRelation.hasReviewFrom(userId)).isTrue();

    // When - Delete all reviews
    productWithRelation.deleteReview(review1.getId());
    productWithRelation.deleteReview(review2.getId());

    // Then - User has no active reviews
    assertThat(productWithRelation.hasReviewFrom(userId)).isFalse();
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentReview() {
    EntityId nonExistentReviewId = new EntityId(UUID.randomUUID());

    assertThatThrownBy(() -> product.updateReview(nonExistentReviewId, 5, "No review exists"))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("Review not found");
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentReview() {
    EntityId nonExistentReviewId = new EntityId(UUID.randomUUID());

    assertThatThrownBy(() -> product.deleteReview(nonExistentReviewId))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("Review not found");
  }

  @Test
  void shouldReturnFalseWhenRemovingNonExistentReview() {
    EntityId nonExistentReviewId = new EntityId(UUID.randomUUID());

    boolean removed = product.removeReview(nonExistentReviewId);

    assertThat(removed).isFalse();
  }

  @Test
  void shouldReturnEmptyListForUserWithNoReviews() {
    List<Review> userReviews = product.findReviewsByUser(userId);
    List<Review> activeReviews = product.findActiveReviewsByUser(userId);

    assertThat(userReviews).isEmpty();
    assertThat(activeReviews).isEmpty();
  }

  @Test
  void shouldReturnEmptyListForUserWithNoRelation() {
    // Even if there are reviews from other users, this user has no relation
    EntityId otherUserId = new EntityId(UUID.randomUUID());
    ProductUserRelation otherRelation = ProductUserRelation.create(productId, otherUserId);

    Product productWithOtherRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(otherRelation));

    productWithOtherRelation.addReviewFromUser(otherUserId, 5, "Other user review");

    // When searching for userId (who has no relation)
    List<Review> userReviews = productWithOtherRelation.findReviewsByUser(userId);
    List<Review> activeReviews = productWithOtherRelation.findActiveReviewsByUser(userId);

    // Then
    assertThat(userReviews).isEmpty();
    assertThat(activeReviews).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenUpdatingDeletedReview() {
    // Given - Create product with review and then delete it
    ProductUserRelation userRelation = ProductUserRelation.create(productId, userId);

    Product productWithRelation = Product.reconstruct(
        productId, "Test Product", "Test Brand", category, "#FFFFFF",
        List.of(), Set.of(userRelation));

    Review review = productWithRelation.addReviewFromUser(userId, 4, "Review to delete");
    productWithRelation.deleteReview(review.getId());

    // When/Then - Try to update the deleted review
    assertThatThrownBy(() -> productWithRelation.updateReview(review.getId(), 5, "Updated"))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("Cannot update a deleted review");
  }
}