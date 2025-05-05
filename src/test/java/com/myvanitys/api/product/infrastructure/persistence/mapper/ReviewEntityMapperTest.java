package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ReviewEntityMapperTest {

    private ReviewEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ReviewEntityMapper.class);
    }

    @Test
    void toProductUserRelation_whenReviewIdIsNull_shouldReturnNull() {
        // Arrange
        ProductUserRelation relation = ProductUserRelation.reconstruct(
            new EntityId(UUID.randomUUID()),
            new EntityId(UUID.randomUUID()),
            new EntityId(UUID.randomUUID()),
            null
        );

        // Act
        ProductUserRelation result = mapper.toProductUserRelation(relation);

        // Assert
        assertNull(result);
    }

    @Test
    void toProductUserRelation_withValidData_shouldMapCorrectly() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ProductUserRelation relation = ProductUserRelation.reconstruct(
            new EntityId(id),
            new EntityId(productId),
            new EntityId(userId),
            new EntityId(reviewId)
        );

        // Act
        ProductUserRelation result = mapper.toProductUserRelation(relation);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId().getValue());
        assertEquals(productId, result.getProductId().getValue());
        assertEquals(userId, result.getUserId().getValue());
        assertEquals(reviewId, result.getReviewId().getValue());
    }

    @Test
    void toReviewEntity_withValidData_shouldMapCorrectly() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID productUserId = UUID.randomUUID();

        EntityId productEntityId = new EntityId(productId);
        EntityId userEntityId = new EntityId(userId);
        EntityId reviewEntityId = new EntityId(reviewId);
        EntityId productUserEntityId = new EntityId(productUserId);

        ProductUserRelation relation = ProductUserRelation.reconstruct(
            productUserEntityId,
            productEntityId,
            userEntityId,
            reviewEntityId
        );
        int rating = 5;
        String comment = "Gran producto";

        // Act
        ReviewEntity result = mapper.toReviewEntity(relation, rating, comment);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getReviewId());
        assertEquals(rating, result.getRating());
        assertEquals(comment, result.getComment());
        assertNotNull(result.getProductUserId());
    }
}