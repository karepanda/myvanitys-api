package com.myvanitys.api.product.infrastructure.persistence.adapter;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewJpaAdapterTest {
    @InjectMocks private ReviewJpaAdapter target;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewMapper reviewMapper;

    EntityId id = new EntityId(UUID.randomUUID());
    Category category = new Category(id, "category");
    Product product = new Product(id, "name", "brand", category , "#FFFFFF");

    @Nested
    class Save {

        @Test
        void when_givenValidReview_then_reviewIsSaved() {
            final EntityId reviewId = new EntityId(UUID.randomUUID());
            final Review review = new Review(reviewId, new EntityId(UUID.randomUUID()),product, 5, "Great product!");

            ReviewEntity reviewEntity = new ReviewEntity(reviewId.getValue(), );

            when(reviewMapper.toEntity(review)).thenReturn(null); // Simulate entity conversion
            when(reviewRepository.save(null)).thenReturn(null); // Simulate save
            when(reviewMapper.toDomain(null)).thenReturn(review); // Simulate conversion back to domain

            final Review result = target.save(review);

            assertThat(result).isEqualTo(review);
        }
    }

    @Nested
    class FindById {
        @Test
        void when_givenExistingId_then_reviewIsReturned() {
            final EntityId reviewId = new EntityId(UUID.randomUUID());
            final Review review = new Review(reviewId, new EntityId(UUID.randomUUID()), product, 4, "Good");

            when(reviewRepository.findById(reviewId.getValue())).thenReturn(Optional.ofNullable(null));
            when(reviewMapper.toDomain(null)).thenReturn(review);

            final Optional<Review> result = target.findById(reviewId);

            assertThat(result).isPresent().contains(review);
        }

        @Test
        void when_givenNonExistingId_then_emptyOptionalIsReturned() {
            final EntityId reviewId = new EntityId(UUID.randomUUID());

            when(reviewRepository.findById(reviewId.getValue())).thenReturn(Optional.empty());

            final Optional<Review> result = target.findById(reviewId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByProductId {
        @Test
        void when_givenProductId_then_returnReviewsList() {
            final EntityId productId = new EntityId(UUID.randomUUID());
            final Review review = new Review(new EntityId(UUID.randomUUID()), new EntityId(UUID.randomUUID()), product, 5, "Amazing!");
            final List<Review> reviews = List.of(review);

            when(reviewRepository.findByProductUserEntityProductId(productId.getValue())).thenReturn(List.of(null));
            when(reviewMapper.toDomain(null)).thenReturn(review);

            final List<Review> result = target.findByProductId(productId);

            assertThat(result).isEqualTo(reviews);
        }

        @Test
        void when_givenProductIdWithoutReviews_then_returnEmptyList() {
            final EntityId productId = new EntityId(UUID.randomUUID());

            when(reviewRepository.findByProductUserEntityProductId(productId.getValue())).thenReturn(List.of());

            final List<Review> result = target.findByProductId(productId);

            assertThat(result).isEqualTo(null);
        }
    }

    @Nested
    class FindByUserId {
        @Test
        void when_givenUserId_then_returnReviewsList() {
            final EntityId userId = new EntityId(UUID.randomUUID());
            final Review review = new Review(new EntityId(UUID.randomUUID()), userId, product, 3, "Decent");
            final List<Review> reviews = List.of(review);

            when(reviewRepository.findByProductUserEntityProductUserId(userId.getValue())).thenReturn(List.of(null));
            when(reviewMapper.toDomain(null)).thenReturn(review);

            final List<Review> result = target.findByUserId(userId);

            assertThat(result).isEqualTo(reviews);
        }

        @Test
        void when_givenUserIdWithoutReviews_then_returnEmptyList() {
            final EntityId userId = new EntityId(UUID.randomUUID());

            when(reviewRepository.findByProductUserEntityProductUserId(userId.getValue())).thenReturn(List.of());

            final List<Review> result = target.findByUserId(userId);

            assertThat(result).isEqualTo(null);
        }
    }
}