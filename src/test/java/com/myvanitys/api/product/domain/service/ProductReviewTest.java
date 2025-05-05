package com.myvanitys.api.product.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
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

//        @Test
//        void when_productsWithActiveAndInactiveReviews_then_calculatesAverageForActiveOnly() {
//            // Given
//           review1 = org.mockito.Mockito.mock(Review.class);
//           review2 = org.mockito.Mockito.mock(Review.class);
//
//            when(review1.isActive()).thenReturn(true);
//            when(review1.getRating()).thenReturn(4);
//
//            when(review2.isActive()).thenReturn(false);
//            when(review2.getRating()).thenReturn(2);
//
//            when(product.getReviews()).thenReturn(Arrays.asList(review1, review2));
//
//            // When
//            double result = productReview.calculateOverallAverageRating(List.of(product));
//
//            // Then
//            assertThat(result).isEqualTo(4);
//        }

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

//    @Nested
//    class FindUserIdForReview {
//
//        @Test
//        void when_reviewIdExists_then_returnsUserId() {
//            // Given
//            EntityId reviewId = new EntityId();
//            EntityId userId = new EntityId();
//            EntityId productUserId = new EntityId();
//
//            ProductUserRelation relation = org.mockito.Mockito.mock(ProductUserRelation.class);
//
//            when(relation.getReviewId()).thenReturn(reviewId);
//            when(relation.getUserId()).thenReturn(userId);
//            when(product.getUserRelations()).thenReturn();
//
//            // When
//            EntityId result = productReview.(product, reviewId);
//
//            // Then
//            assertThat(result).isEqualTo(userId);
//        }
//
//        @Test
//        void when_reviewIdDoesNotExist_then_returnsNull() {
//            // Given
//            EntityId nonExistentReviewId = new EntityId();
//
//
//            // When
//            EntityId result = productReview.findUserIdForReview(product, nonExistentReviewId);
//
//            // Then
//            assertThat(result).isNull();
//        }
//    }
}