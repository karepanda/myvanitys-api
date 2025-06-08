package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
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
  void shouldAddReviewFromUser() {
    // When
    Review review = product.addReviewFromUser(userId, 4, "Great product");

    // Then
    assertThat(product.getReviews()).hasSize(1);
    assertThat(product.getReviews().get(0)).isEqualTo(review);
    assertThat(product.getAverageRating()).isEqualTo(4);

    // User relation should be created
    assertThat(product.getUserRelations()).hasSize(1);
    Optional<ProductUserRelation> relation = product.getUserRelations().stream().findFirst();
    assertThat(relation).isPresent();
    assertThat(relation.get().getUserId()).isEqualTo(userId);
    assertThat(relation.get().getProductId()).isEqualTo(productId);
    // ❌ REMOVED: No more reviewId in relation
    // assertThat(relation.get().getReviewId()).isEqualTo(review.getId());
  }

  @Test
  void shouldAddMultipleReviewsFromSameUser() {
    // When - User can add multiple reviews
    Review review1 = product.addReviewFromUser(userId, 4, "First review");
    Review review2 = product.addReviewFromUser(userId, 5, "Second review");

    // Then
    assertThat(product.getReviews()).hasSize(2);
    assertThat(product.getReviews()).contains(review1, review2);
    assertThat(product.getAverageRating()).isEqualTo(5); // (4+5)/2 = 4.5 rounded to 5

    // Only one user relation should exist
    assertThat(product.getUserRelations()).hasSize(1);

    // User should have multiple reviews
    List<Review> userReviews = product.findReviewsByUser(userId);
    assertThat(userReviews).hasSize(2);
    assertThat(userReviews).contains(review1, review2);
  }

  @Test
  void shouldUpdateExistingReview() {
    // Given
    Review review = product.addReviewFromUser(userId, 3, "Initial review");
    EntityId reviewId = review.getId();

    // When - Update by review ID, not user ID
    Review updatedReview = product.updateReview(reviewId, 5, "Updated review");

    // Then
    assertThat(updatedReview).isEqualTo(review); // Same review object, just updated
    assertThat(updatedReview.getRating()).isEqualTo(5);
    assertThat(updatedReview.getComment()).isEqualTo("Updated review");
    assertThat(product.getAverageRating()).isEqualTo(5);
  }

  @Test
  void shouldSoftDeleteReview() {
    // Given
    Review review = product.addReviewFromUser(userId, 4, "Review to delete");
    assertThat(product.getAverageRating()).isEqualTo(4);

    // When - Delete by review ID, not user ID
    Review deletedReview = product.deleteReview(review.getId());

    // Then
    assertThat(deletedReview).isEqualTo(review);
    assertThat(deletedReview.isDeleted()).isTrue();

    // The review is still in the collection but marked as deleted
    assertThat(product.getReviews()).hasSize(1);

    // The average rating should exclude deleted reviews
    assertThat(product.getAverageRating()).isEqualTo(0);
  }

  @Test
  void shouldHardDeleteReview() {
    // Given
    Review review = product.addReviewFromUser(userId, 4, "Review to delete");
    assertThat(product.getReviews()).hasSize(1);

    // When - Remove by review ID, not user ID
    boolean removed = product.removeReview(review.getId());

    // Then
    assertThat(removed).isTrue();
    assertThat(product.getReviews()).isEmpty();
    assertThat(product.getAverageRating()).isEqualTo(0);

    // The user relation should still exist
    assertThat(product.getUserRelations()).hasSize(1);
  }

  @Test
  void shouldFindReviewsByUser() {
    // Given
    Review review1 = product.addReviewFromUser(userId, 4, "First review");
    Review review2 = product.addReviewFromUser(userId, 5, "Second review");

    // When
    List<Review> userReviews = product.findReviewsByUser(userId);

    // Then
    assertThat(userReviews).hasSize(2);
    assertThat(userReviews).contains(review1, review2);
  }

  @Test
  void shouldFindActiveReviewsByUser() {
    // Given
    Review review1 = product.addReviewFromUser(userId, 4, "First review");
    Review review2 = product.addReviewFromUser(userId, 5, "Second review");

    // Delete one review
    product.deleteReview(review1.getId());

    // When
    List<Review> activeReviews = product.findActiveReviewsByUser(userId);

    // Then
    assertThat(activeReviews).hasSize(1);
    assertThat(activeReviews).contains(review2);
    assertThat(activeReviews).doesNotContain(review1);
  }

  @Test
  void shouldCalculateAverageRatingCorrectly() {
    // Given
    EntityId user1 = new EntityId(UUID.randomUUID());
    EntityId user2 = new EntityId(UUID.randomUUID());
    EntityId user3 = new EntityId(UUID.randomUUID());

    Review review1 = product.addReviewFromUser(user1, 5, "Excellent");
    Review review2 = product.addReviewFromUser(user2, 3, "Average");
    Review review3 = product.addReviewFromUser(user3, 4, "Good");

    // When
    product.calculateAverageRating();

    // Then
    assertThat(product.getAverageRating()).isEqualTo(4); // (5+3+4)/3 = 4

    // Soft delete one review
    product.deleteReview(review2.getId());

    // Recalculate
    product.calculateAverageRating();

    // Average should now exclude the deleted review
    assertThat(product.getAverageRating()).isEqualTo(5); // (5+4)/2 = 4.5 rounded to 5
  }

  @Test
  void shouldCheckIfUserHasReviews() {
    // Given - Initially no reviews
    assertThat(product.hasReviewFrom(userId)).isFalse();

    // When - Add a review
    product.addReviewFromUser(userId, 4, "Test review");

    // Then
    assertThat(product.hasReviewFrom(userId)).isTrue();

    // When - Add another review (same user)
    product.addReviewFromUser(userId, 5, "Another review");

    // Then - Still true
    assertThat(product.hasReviewFrom(userId)).isTrue();
  }

  @Test
  void shouldHandleDeletedReviewsInHasReviewFrom() {
    // Given
    Review review1 = product.addReviewFromUser(userId, 4, "First review");
    Review review2 = product.addReviewFromUser(userId, 5, "Second review");

    assertThat(product.hasReviewFrom(userId)).isTrue();

    // When - Delete all reviews
    product.deleteReview(review1.getId());
    product.deleteReview(review2.getId());

    // Then - User has no active reviews
    assertThat(product.hasReviewFrom(userId)).isFalse();
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
  void shouldAllowReviewAfterUserRelationExists() {
    // Given - Create user relation first (simulate user interaction with product)
    product.addReviewFromUser(userId, 3, "Initial review");

    // User relation now exists
    assertThat(product.getUserRelations()).hasSize(1);

    // When - Add more reviews (uses existing relation)
    Review review2 = product.addReviewFromUser(userId, 4, "Second review");
    Review review3 = product.addReviewFromUser(userId, 5, "Third review");

    // Then
    assertThat(product.getReviews()).hasSize(3);
    assertThat(product.getUserRelations()).hasSize(1); // Still only one relation

    List<Review> userReviews = product.findReviewsByUser(userId);
    assertThat(userReviews).hasSize(3);
  }
}