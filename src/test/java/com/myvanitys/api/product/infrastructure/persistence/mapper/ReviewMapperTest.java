package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private final ReviewMapper target = Mappers.getMapper(ReviewMapper.class);


    @Nested
    class ToDomain {
        @Test
        void when_givenReviewEntity_then_returnReview() {
            // Given
            final UUID reviewId = UUID.randomUUID();
            final UUID userId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            final int rating = 5;
            final String comment = "Great product!";

            final ProductEntity productEntity = mock(ProductEntity.class);
            final ProductUserEntity productUserEntity = mock(ProductUserEntity.class);
            final ReviewEntity entity = mock(ReviewEntity.class);

            // Configurar los mocks
            when(entity.getReviewId()).thenReturn(reviewId);
            when(entity.getRating()).thenReturn(rating);
            when(entity.getComment()).thenReturn(comment);
            when(entity.getProductUserEntity()).thenReturn(productUserEntity);
            when(productUserEntity.getUserId()).thenReturn(userId);
            when(productUserEntity.getProductId()).thenReturn(productId);
            when(productEntity.getProductId()).thenReturn(productId);


            // Configurar mappers
            EntityId reviewEntityId = new EntityId(reviewId);
            EntityId userEntityId = new EntityId(userId);
            Product product = mock(Product.class);

            when(entityIdMapper.toEntityId(reviewId)).thenReturn(reviewEntityId);
            when(entityIdMapper.toEntityId(userId)).thenReturn(userEntityId);
            when(productMapper.toDomain(productEntity)).thenReturn(product);

            // When
            final Review result = target.toDomain(entity);

            // Then
            assertThat(result)
                    .isNotNull()
                    .extracting(Review::getId, Review::getUserId, Review::getProduct, Review::getRating, Review::getComment)
                    .containsExactly(reviewEntityId, userEntityId, product, rating, comment);
        }
    }
}