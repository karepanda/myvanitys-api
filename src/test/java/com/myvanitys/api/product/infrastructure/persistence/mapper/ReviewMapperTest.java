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
            final int rating = 5;
            final String comment = "Great product!";

            final ProductEntity productEntity = mock(ProductEntity.class);
            final ProductUserEntity productUserEntity = mock(ProductUserEntity.class);
            final ReviewEntity entity = mock(ReviewEntity.class);

            doReturn(userId).when(productUserEntity).getUserId();
            doReturn(productUserEntity).when(entity).getProductUserEntity();
            doReturn(reviewId).when(entity).getReviewId();
            doReturn(rating).when(entity).getRating();
            doReturn(comment).when(entity).getComment();

            final EntityId reviewEntityId = new EntityId(reviewId);
            final EntityId userEntityId = new EntityId(userId);
            final Product product = mock(Product.class);

            doReturn(reviewEntityId).when(entityIdMapper).toEntityId(reviewId);
            doReturn(userEntityId).when(entityIdMapper).toEntityId(userId);
            doReturn(product).when(productMapper).toDomain(productEntity);

            System.out.println("entity.getUserEntity(): " + entity.getProductUserEntity());
            System.out.println("entity.getUserEntity().getId(): " + (entity.getProductUserEntity() != null ? entity.getProductUserEntity().getUserId() : "null"));
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