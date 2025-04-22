package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

    @InjectMocks
    private ReviewMapper reviewMapper;

    private UUID reviewId;
    private UUID userId;
    private Product mockProduct;
    private ProductUserEntity productUserEntity;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        userId = UUID.randomUUID();
        mockProduct = mock(Product.class);
        
        productUserEntity = new ProductUserEntity();
        productUserEntity.setUserId(userId);
    }

    @Test
    void toDomain_shouldMapReviewEntityToReview() {
        // Arrange
        ReviewEntity reviewEntity = ReviewEntity.builder()
                .reviewId(reviewId)
                .rating(5)
                .comment("Excelente producto")
                .productUserEntity(productUserEntity)
                .build();

        // Act
        Review result = reviewMapper.toDomain(reviewEntity, mockProduct);

        // Assert
        assertNotNull(result);
        assertEquals(reviewId, result.getId().getValue());
        assertEquals(userId, result.getUserId().getValue());
        assertEquals(mockProduct, result.getProduct());
        assertEquals(5, result.getRating());
        assertEquals("Excelente producto", result.getComment());
    }

    @Test
    void toDomain_shouldReturnNullWhenEntityIsNull() {
        // Act
        Review result = reviewMapper.toDomain(null, mockProduct);

        // Assert
        assertNull(result);
    }

    @Test
    void toDomain_shouldThrowExceptionWhenProductIsNull() {
        // Arrange
        ReviewEntity reviewEntity = ReviewEntity.builder()
                .reviewId(reviewId)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reviewMapper.toDomain(reviewEntity, null),
                "Product cannot be null for review conversion");
    }

    @Test
    void toEntity_shouldMapReviewToReviewEntity() {
        // Arrange
        Review review = new Review(
                new EntityId(reviewId),
                new EntityId(userId),
                mockProduct,
                4,
                "Buen producto"
        );

        // Act
        ReviewEntity result = reviewMapper.toEntity(review);

        // Assert
        assertNotNull(result);
        assertEquals(reviewId, result.getReviewId());
        assertEquals(4, result.getRating());
        assertEquals("Buen producto", result.getComment());
    }

    @Test
    void toEntity_shouldReturnNullWhenDomainIsNull() {
        // Act
        ReviewEntity result = reviewMapper.toEntity(null);

        // Assert
        assertNull(result);
    }
}