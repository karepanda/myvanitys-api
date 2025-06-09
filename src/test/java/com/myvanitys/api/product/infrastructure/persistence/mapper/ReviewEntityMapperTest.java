package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewEntityMapperTest {

  private ReviewEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ReviewEntityMapper() {
    };
  }

  @Test
  void toProductUserRelation_withValidProductUserEntity_shouldMapCorrectly() {
    // Arrange
    UUID productUserId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ProductUserEntity entity = new ProductUserEntity();
    entity.setProductUserId(productUserId);
    entity.setProductId(productId);
    entity.setUserId(userId);

    // Act
    ProductUserRelation result = mapper.toProductUserRelation(entity);

    // Assert
    assertNotNull(result);
    assertEquals(productUserId, result.getId().getValue());
    assertEquals(productId, result.getProductId().getValue());
    assertEquals(userId, result.getUserId().getValue());
  }

  @Test
  void toProductUserRelation_withNullEntity_shouldReturnNull() {
    // Act
    ProductUserRelation result = mapper.toProductUserRelation((ProductUserEntity) null);

    // Assert
    assertNull(result);
  }

  @Test
  void toProductUserEntity_withValidRelation_shouldMapCorrectly() {
    // Arrange
    UUID productUserId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ProductUserRelation relation = ProductUserRelation.reconstruct(
        new EntityId(productUserId),
        new EntityId(productId),
        new EntityId(userId)
    );

    // Act
    ProductUserEntity result = mapper.toProductUserEntity(relation);

    // Assert
    assertNotNull(result);
    assertEquals(productUserId, result.getProductUserId());
    assertEquals(productId, result.getProductId());
    assertEquals(userId, result.getUserId());
  }

  @Test
  void toProductUserEntity_withNullRelation_shouldReturnNull() {
    // Act
    ProductUserEntity result = mapper.toProductUserEntity(null);

    // Assert
    assertNull(result);
  }

  @Test
  void createReviewEntity_withValidData_shouldMapCorrectly() {
    // Arrange
    UUID productUserId = UUID.randomUUID();
    EntityId productUserEntityId = new EntityId(productUserId);
    int rating = 5;
    String comment = "Gran producto";

    // Act
    ReviewEntity result = mapper.createReviewEntity(productUserEntityId, rating, comment);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getReviewId());
    assertEquals(rating, result.getRating());
    assertEquals(comment, result.getComment());
    assertEquals(productUserId, result.getProductUserId());
  }

  @Test
  void createReviewEntity_withNullProductUserId_shouldThrowException() {
    // Arrange
    int rating = 5;
    String comment = "Gran producto";

    // Act & Assert
    assertThrows(NullPointerException.class, () ->
        mapper.createReviewEntity(null, rating, comment));
  }

  @Test
  void createReviewEntity_withInvalidRating_shouldStillCreate() {
    // Arrange
    EntityId productUserId = new EntityId(UUID.randomUUID());
    int invalidRating = 0;
    String comment = "Comment";

    // Act
    ReviewEntity result = mapper.createReviewEntity(productUserId, invalidRating, comment);

    // Assert
    assertNotNull(result);
    assertEquals(invalidRating, result.getRating());
    assertEquals(comment, result.getComment());
  }

  @Test
  void createReviewEntity_withNullComment_shouldCreateWithNullComment() {
    // Arrange
    EntityId productUserId = new EntityId(UUID.randomUUID());
    int rating = 5;

    // Act
    ReviewEntity result = mapper.createReviewEntity(productUserId, rating, null);

    // Assert
    assertNotNull(result);
    assertEquals(rating, result.getRating());
    assertNull(result.getComment());
    assertEquals(productUserId.getValue(), result.getProductUserId());
  }

  @Test
  void createReviewEntity_withNullComment_shouldThrowException() {
    // Arrange
    int rating = 5;
    String comment = "Gran producto";

    // Act & Assert
    assertThrows(NullPointerException.class, () ->
        mapper.createReviewEntity(null, rating, comment));
  }
}