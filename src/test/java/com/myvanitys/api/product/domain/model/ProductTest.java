package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    assertThat(relation.get().getReviewId()).isEqualTo(review.getId());
  }

  @Test
  void shouldUpdateExistingReview() {
    // Given
    Review review = product.addReviewFromUser(userId, 3, "Initial review");

    // When
    Review updatedReview = product.updateReview(userId, 5, "Updated review");

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

    // When
    Review deletedReview = product.deleteReview(userId);

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
    product.addReviewFromUser(userId, 4, "Review to delete");
    assertThat(product.getReviews()).hasSize(1);

    // When
    boolean removed = product.removeReviewByUser(userId);

    // Then
    assertThat(removed).isTrue();
    assertThat(product.getReviews()).isEmpty();
    assertThat(product.getAverageRating()).isEqualTo(0);

    // The user relation should still exist but without a review ID
    assertThat(product.getUserRelations()).hasSize(1);
    Optional<ProductUserRelation> relation = product.getUserRelations().stream().findFirst();
    assertThat(relation).isPresent();
    assertThat(relation.get().getReviewId()).isNull();
  }

  @Test
  void shouldFindReviewByUser() {
    // Given
    Review review = product.addReviewFromUser(userId, 4, "Test review");

    // When
    Optional<Review> foundReview = product.findReviewByUser(userId);

    // Then
    assertThat(foundReview).isPresent();
    assertThat(foundReview.get()).isEqualTo(review);
  }

  @Test
  void shouldCalculateAverageRatingCorrectly() {
    // Given
    EntityId user1 = new EntityId(UUID.randomUUID());
    EntityId user2 = new EntityId(UUID.randomUUID());
    EntityId user3 = new EntityId(UUID.randomUUID());

    product.addReviewFromUser(user1, 5, "Excellent");
    product.addReviewFromUser(user2, 3, "Average");
    product.addReviewFromUser(user3, 4, "Good");

    // When
    product.calculateAverageRating();

    // Then
    assertThat(product.getAverageRating()).isEqualTo(4); // (5+3+4)/3 = 4

    // Soft delete one review
    product.deleteReview(user2);

    // Recalculate
    product.calculateAverageRating();

    // Average should now exclude the deleted review
    assertThat(product.getAverageRating()).isEqualTo(5); // (5+4)/2 = 4.5 rounded to 5
  }

  @Test
  void shouldPreventDuplicateReviewsByUser() {
    // Given
    product.addReviewFromUser(userId, 4, "First review");

    // When/Then
    assertThatThrownBy(() -> product.addReviewFromUser(userId, 5, "Second review"))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("User already has a review for this product");
  }

  @Test
  void shouldAllowAddingReviewAfterDeletion() {
    // Given
    product.addReviewFromUser(userId, 3, "Original review");
    product.deleteReview(userId);

    // When
    Review newReview = product.addReviewFromUser(userId, 5, "New review after deletion");

    // Then
    assertThat(newReview).isNotNull();
    assertThat(newReview.getRating()).isEqualTo(5);
    assertThat(newReview.getComment()).isEqualTo("New review after deletion");
    assertThat(product.getAverageRating()).isEqualTo(5);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentReview() {
    assertThatThrownBy(() -> product.updateReview(userId, 5, "No review exists"))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("User has no relation with this product");
  }

  @Test
  void shouldNotThrowExceptionWhenDeletingNonExistentReview() {
    // When
    Review deletedReview = product.deleteReview(userId);

    // Then
    assertThatThrownBy(() -> product.deleteReview(userId))
        .isInstanceOf(ReviewValidationException.class)
        .hasMessageContaining("User has no relation with this product");
  }
}