package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {
    @Mock
    private ProductMapper productMapper;

    @Mock
    private EntityIdMapper entityIdMapper;

    private final ReviewMapper target = ReviewMapper.INSTANCE;

    private UUID reviewId;
    private UUID userId;
    private int rating;
    private String comment;
    private ReviewEntity entity;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        rating = 5;
        comment = "Great product!";

        entity = new ReviewEntity();
        entity.setReviewId(reviewId);
        entity.setRating(rating);
        entity.setComment(comment);

        ProductUserEntity productUserEntity = new ProductUserEntity();
        productUserEntity.setUserId(userId);
        productUserEntity.setProductId(productId);
        entity.setProductUserEntity(productUserEntity);
    }

    @Nested
    class ToDomain {
        @Test
        void when_givenValidReviewEntity_then_returnMappedReview() {
            // Arrange
            EntityId reviewEntityId = new EntityId(reviewId);
            EntityId userEntityId = new EntityId(userId);
            Product product = mock(Product.class);

            when(entityIdMapper.toEntityId(reviewId)).thenReturn(reviewEntityId);
            when(entityIdMapper.toEntityId(userId)).thenReturn(userEntityId);
            when(productMapper.toDomain(any())).thenReturn(product);

            // Act
            final Review result = target.toDomain(entity);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .extracting(Review::getId, Review::getUserId, Review::getProduct, Review::getRating, Review::getComment)
                    .containsExactly(reviewEntityId, userEntityId, product, rating, comment);
        }

        @Test
        void when_givenNullReviewEntity_then_returnNull() {
            // Act
            final Review result = target.toDomain(null);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        void when_givenReviewEntityWithNullProductUser_then_handleGracefully() {
            // Arrange
            entity.setProductUserEntity(null);

            // Act
            final Review result = target.toDomain(entity);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isNull();
            assertThat(result.getProduct()).isNull();
        }

        @Test
        void when_givenReviewEntityWithNullValues_then_handleGracefully() {
            // Arrange
            entity.setReviewId(null);
            entity.setRating(0);
            entity.setComment(null);
            entity.getProductUserEntity().setUserId(null);
            entity.getProductUserEntity().setProductId(null);

            when(entityIdMapper.toEntityId(null)).thenReturn(null);
            when(productMapper.toDomain(null)).thenReturn(null);

            // Act
            final Review result = target.toDomain(entity);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getUserId()).isNull();
            assertThat(result.getProduct()).isNull();
            assertThat(result.getRating()).isEqualTo(0);
            assertThat(result.getComment()).isNull();
        }
    }
}