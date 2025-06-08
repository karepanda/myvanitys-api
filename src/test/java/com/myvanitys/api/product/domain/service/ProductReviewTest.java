package com.myvanitys.api.product.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductReviewTest {

  @InjectMocks
  private ProductReview productReview;

  @Mock
  private Product product;

  @Mock
  private Review review1;

  @Mock
  private Review review2;

  @Nested
  class CalculateOverallAverageRating {

    @Test
    void when_noProducts_then_returnsZero() {
      // When
      double result = productReview.calculateOverallAverageRating(Collections.emptyList());

      // Then
      assertThat(result).isZero();
    }

    @Test
    void when_productsWithNoReviews_then_returnsZero() {
      // Given
      when(product.getReviews()).thenReturn(Collections.emptyList());

      // When
      double result = productReview.calculateOverallAverageRating(List.of(product));

      // Then
      assertThat(result).isZero();
    }

    @Test
    void when_productsWithActiveAndInactiveReviews_then_calculatesAverageForActiveOnly() {
      // Given
      when(review1.isActive()).thenReturn(true);
      when(review1.getRating()).thenReturn(4);

      when(review2.isActive()).thenReturn(false);

      when(product.getReviews()).thenReturn(Arrays.asList(review1, review2));

      // When
      double result = productReview.calculateOverallAverageRating(List.of(product));

      // Then
      assertThat(result).isEqualTo(4.0);
    }

    @Test
    void when_multipleProductsWithReviews_then_calculatesOverallAverage() {
      // Given
      Product product2 = org.mockito.Mockito.mock(Product.class);
      Review review3 = org.mockito.Mockito.mock(Review.class);

      when(review1.isActive()).thenReturn(true);
      when(review1.getRating()).thenReturn(5);

      when(review2.isActive()).thenReturn(true);
      when(review2.getRating()).thenReturn(3);

      when(review3.isActive()).thenReturn(true);
      when(review3.getRating()).thenReturn(4);

      when(product.getReviews()).thenReturn(List.of(review1, review2));
      when(product2.getReviews()).thenReturn(List.of(review3));

      // When
      double result = productReview.calculateOverallAverageRating(Arrays.asList(product, product2));

      // Then
      assertThat(result).isEqualTo(4.0); // (5 + 3 + 4) / 3 = 4.0
    }
  }

  @Nested
  class FindUserIdForReview {

    @Test
    void when_reviewIdExists_then_returnsUserId() {
      // Given
      EntityId reviewId = new EntityId(UUID.randomUUID());
      EntityId userId = new EntityId(UUID.randomUUID());
      EntityId productUserId = new EntityId(UUID.randomUUID());

      Review review = org.mockito.Mockito.mock(Review.class);
      ProductUserRelation relation = org.mockito.Mockito.mock(ProductUserRelation.class);

      when(review.getProductUserId()).thenReturn(productUserId);
      when(relation.getId()).thenReturn(productUserId);
      when(relation.getUserId()).thenReturn(userId);

      when(product.findReviewById(reviewId)).thenReturn(Optional.of(review));
      when(product.getUserRelations()).thenReturn(Set.of(relation));

      // When
      EntityId result = productReview.findUserIdForReview(product, reviewId);

      // Then
      assertThat(result).isEqualTo(userId);
    }

    @Test
    void when_reviewIdDoesNotExist_then_returnsNull() {
      // Given
      EntityId nonExistentReviewId = new EntityId(UUID.randomUUID());

      when(product.findReviewById(nonExistentReviewId)).thenReturn(Optional.empty());

      // When
      EntityId result = productReview.findUserIdForReview(product, nonExistentReviewId);

      // Then
      assertThat(result).isNull();
    }

    @Test
    void when_reviewExistsButRelationNotFound_then_returnsNull() {
      // Given
      EntityId reviewId = new EntityId(UUID.randomUUID());
      EntityId productUserId = new EntityId(UUID.randomUUID());

      Review review = org.mockito.Mockito.mock(Review.class);

      when(review.getProductUserId()).thenReturn(productUserId);
      when(product.findReviewById(reviewId)).thenReturn(Optional.of(review));
      when(product.getUserRelations()).thenReturn(Collections.emptySet());

      // When
      EntityId result = productReview.findUserIdForReview(product, reviewId);

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  class GetUserReviewsAcrossProducts {

    @Test
    void when_userHasReviewsInMultipleProducts_then_returnsAllReviews() {
      // Given
      EntityId userId = new EntityId(UUID.randomUUID());
      Product product2 = org.mockito.Mockito.mock(Product.class);

      when(product.findReviewsByUser(userId)).thenReturn(List.of(review1));
      when(product2.findReviewsByUser(userId)).thenReturn(List.of(review2));

      // When
      List<Review> result = productReview.getUserReviewsAcrossProducts(
          List.of(product, product2), userId);

      // Then
      assertThat(result).containsExactly(review1, review2);
    }

    @Test
    void when_userHasNoReviews_then_returnsEmptyList() {
      // Given
      EntityId userId = new EntityId(UUID.randomUUID());

      when(product.findReviewsByUser(userId)).thenReturn(Collections.emptyList());

      // When
      List<Review> result = productReview.getUserReviewsAcrossProducts(
          List.of(product), userId);

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class GetUserActiveReviewsAcrossProducts {

    @Test
    void when_userHasActiveReviews_then_returnsOnlyActiveOnes() {
      // Given
      EntityId userId = new EntityId(UUID.randomUUID());
      Product product2 = org.mockito.Mockito.mock(Product.class);

      when(product.findActiveReviewsByUser(userId)).thenReturn(List.of(review1));
      when(product2.findActiveReviewsByUser(userId)).thenReturn(Collections.emptyList());

      // When
      List<Review> result = productReview.getUserActiveReviewsAcrossProducts(
          List.of(product, product2), userId);

      // Then
      assertThat(result).containsExactly(review1);
    }
  }

  @Nested
  class CalculateUserAverageRating {

    @Test
    void when_userHasActiveReviews_then_calculatesAverage() {
      // Given
      EntityId userId = new EntityId(UUID.randomUUID());
      Review review3 = org.mockito.Mockito.mock(Review.class);

      when(review1.getRating()).thenReturn(4);
      when(review2.getRating()).thenReturn(5);
      when(review3.getRating()).thenReturn(3);

      when(product.findActiveReviewsByUser(userId)).thenReturn(List.of(review1, review2, review3));

      // When
      double result = productReview.calculateUserAverageRating(List.of(product), userId);

      // Then
      assertThat(result).isEqualTo(4.0); // (4 + 5 + 3) / 3 = 4.0
    }

    @Test
    void when_userHasNoActiveReviews_then_returnsZero() {
      // Given
      EntityId userId = new EntityId(UUID.randomUUID());

      when(product.findActiveReviewsByUser(userId)).thenReturn(Collections.emptyList());

      // When
      double result = productReview.calculateUserAverageRating(List.of(product), userId);

      // Then
      assertThat(result).isZero();
    }
  }

  @Nested
  class GetProductStats {

    @Test
    void when_productHasActiveReviews_then_returnsCorrectStats() {
      // Given
      when(review1.isActive()).thenReturn(true);
      when(review1.getRating()).thenReturn(4);

      when(review2.isActive()).thenReturn(true);
      when(review2.getRating()).thenReturn(5);

      when(product.getReviews()).thenReturn(List.of(review1, review2));
      when(product.getAverageRating()).thenReturn(5); // Rounded value from Product

      // When
      ProductReview.ProductReviewStats result = productReview.getProductStats(product);

      // Then
      assertThat(result.getTotalReviews()).isEqualTo(2);
      assertThat(result.getExactAverageRating()).isEqualTo(4.5);
      assertThat(result.getRoundedAverageRating()).isEqualTo(5);
    }

    @Test
    void when_productHasNoReviews_then_returnsZeroStats() {
      // Given
      when(product.getReviews()).thenReturn(Collections.emptyList());
      when(product.getAverageRating()).thenReturn(0);

      // When
      ProductReview.ProductReviewStats result = productReview.getProductStats(product);

      // Then
      assertThat(result.getTotalReviews()).isZero();
      assertThat(result.getExactAverageRating()).isZero();
      assertThat(result.getRoundedAverageRating()).isZero();
    }
  }
}