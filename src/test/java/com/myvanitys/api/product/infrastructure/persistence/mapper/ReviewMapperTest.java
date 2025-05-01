package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
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

  private UUID productUserId;

  private ProductUserEntity productUserEntity;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    productUserEntity = new ProductUserEntity();
    productUserEntity.setUserId(userId);
  }

  @Test
  void toDomain_shouldMapReviewEntityToReview() {
    // Arrange
    productUserId = UUID.randomUUID();
    EntityId productUserEntityId = new EntityId(productUserId);
    ReviewEntity reviewEntity = ReviewEntity.builder()
        .reviewId(reviewId)
        .productUserId(productUserId)
        .rating(5)
        .comment("Excelente producto")
        .build();

    // Act
    Review result = reviewMapper.toDomain(reviewEntity, productUserEntityId);

    // Assert
    assertNotNull(result);
    assertEquals(reviewId, result.getId().getValue());
    assertEquals(userId, result.getProductUserId().getValue());
    assertEquals(productUserId, result.getProductUserId().getValue());
    assertEquals(5, result.getRating());
    assertEquals("Excelente producto", result.getComment());
  }

  @Test
  void toDomain_shouldReturnNullWhenEntityIsNull() {
    // Act
    productUserId = UUID.randomUUID();
    EntityId productUserEntityId = new EntityId(productUserId);
    Review result = reviewMapper.toDomain(null, productUserEntityId);

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
    productUserId = UUID.randomUUID();
    EntityId productUserEntityId = new EntityId(productUserId);
    ReviewDetails reviewDetails = ReviewDetails.of(
        4,
        "Buen producto",
        null,
        null,
        null);

    Review review = Review.createWithExistingId(EntityId.newId(), productUserEntityId, reviewDetails);

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