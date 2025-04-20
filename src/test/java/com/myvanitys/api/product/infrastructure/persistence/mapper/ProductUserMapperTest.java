package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductUserMapperTest {

  @Mock
  private EntityIdMapper entityIdMapper;

  @InjectMocks
  private ProductUserMapperImpl productUserMapper;

  private UUID productUserId;

  private UUID productId;

  private UUID userId;

  private UUID reviewId;

  @BeforeEach
  void setUp() {
    productUserId = UUID.randomUUID();
    productId = UUID.randomUUID();
    userId = UUID.randomUUID();
    reviewId = UUID.randomUUID();

    // Mock EntityIdMapper behavior
    lenient().when(entityIdMapper.toEntityId(any(UUID.class))).thenAnswer(invocation -> {
      UUID uuid = invocation.getArgument(0);
      return uuid != null ? new EntityId(uuid) : null;
    });

    lenient().when(entityIdMapper.toUUID(any(EntityId.class))).thenAnswer(invocation -> {
      EntityId entityId = invocation.getArgument(0);
      return entityId != null ? entityId.getValue() : null;
    });
  }

  @Test
  void toDomain_shouldMapProductUserEntityToProductUserRelation() {
    // Arrange
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserId);
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);

    // Act
    ProductUserRelation result = productUserMapper.toDomain(productUserEntity);

    // Assert
    assertNotNull(result);
    assertEquals(productUserId, result.getId().getValue());
    assertEquals(productId, result.getProductId().getValue());
    assertEquals(userId, result.getUserId().getValue());
    assertNull(result.getReviewId());
  }

  @Test
  void toDomain_whenEntityIsNull_shouldReturnNull() {
    // Act
    ProductUserRelation result = productUserMapper.toDomain(null);

    // Assert
    assertNull(result);
  }

  @Test
  void toEntity_shouldMapProductUserRelationToProductUserEntity() {
    // Arrange
    ProductUserRelation productUserRelation = new ProductUserRelation(
        new EntityId(productUserId),
        new EntityId(productId),
        new EntityId(userId),
        new EntityId(reviewId)
    );

    // Act
    ProductUserEntity result = productUserMapper.toEntity(productUserRelation);

    // Assert
    assertNotNull(result);
    assertEquals(productUserId, result.getProductUserId());
    assertEquals(productId, result.getProductId());
    assertEquals(userId, result.getUserId());
    assertNull(result.getReviews());
    assertNull(result.getCreatedAt());
    assertNull(result.getUpdatedAt());
  }

  @Test
  void toEntity_whenRelationIsNull_shouldReturnNull() {
    // Act
    ProductUserEntity result = productUserMapper.toEntity(null);

    // Assert
    assertNull(result);
  }

  @Test
  void toEntity_shouldIgnoreReviews() {
    // Arrange
    ProductUserRelation productUserRelation = new ProductUserRelation(
        new EntityId(productUserId),
        new EntityId(productId),
        new EntityId(userId),
        new EntityId(reviewId)
    );

    // Act
    ProductUserEntity result = productUserMapper.toEntity(productUserRelation);

    // Assert
    assertNotNull(result);
    assertNull(result.getReviews(), "Reviews should be ignored in mapping");
  }

  @Test
  void toEntity_shouldIgnoreTimestamps() {
    // Arrange
    ProductUserRelation productUserRelation = new ProductUserRelation(
        new EntityId(productUserId),
        new EntityId(productId),
        new EntityId(userId),
        null
    );

    // Act
    ProductUserEntity result = productUserMapper.toEntity(productUserRelation);

    // Assert
    assertNotNull(result);
    assertNull(result.getCreatedAt(), "CreatedAt should be ignored in mapping");
    assertNull(result.getUpdatedAt(), "UpdatedAt should be ignored in mapping");
  }

  @Test
  void toDomain_withNullReviewId_shouldMapCorrectly() {
    // Arrange
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserId);
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);
    productUserEntity.setReviews(null);  // No reviews

    // Act
    ProductUserRelation result = productUserMapper.toDomain(productUserEntity);

    // Assert
    assertNotNull(result);
    assertEquals(productUserId, result.getId().getValue());
    assertEquals(productId, result.getProductId().getValue());
    assertEquals(userId, result.getUserId().getValue());
    assertNull(result.getReviewId());
  }

  @Test
  void toDomain_withEmptyReviewsList_shouldReturnNullReviewId() {
    // Arrange
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserId);
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);
    productUserEntity.setReviews(new ArrayList<>());  // Empty reviews list

    // Act
    ProductUserRelation result = productUserMapper.toDomain(productUserEntity);

    // Assert
    assertNotNull(result);
    assertNull(result.getReviewId());
  }
}