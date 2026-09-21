package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductUserRelationMapperTest {

  private static final UUID RELATION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID OTHER_RELATION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID OTHER_PRODUCT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID OTHER_USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID REVIEW_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

  private static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");
  private static final Instant PREVIOUS_UPDATED_AT = Instant.parse("2024-02-01T10:00:00Z");
  private static final Instant DELETED_AT = Instant.parse("2024-03-01T10:00:00Z");

  private ProductUserRelationMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ProductUserRelationMapper.class);
  }

  @Test
  void toDomain_withValidEntity_shouldMapIdsIndependently() {
    ProductUserEntity entity = entity(RELATION_ID, PRODUCT_ID, USER_ID);

    ProductUserRelation result = mapper.toDomain(entity);

    assertNotNull(result);
    assertEquals(RELATION_ID, result.getId().getValue());
    assertEquals(PRODUCT_ID, result.getProductId().getValue());
    assertEquals(USER_ID, result.getUserId().getValue());
  }

  @Test
  void toDomain_withNullEntity_shouldReturnNull() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toEntity_withValidRelation_shouldMapIdsAndGenerateTimestamps() {
    ProductUserRelation relation = relation(RELATION_ID, PRODUCT_ID, USER_ID);

    Instant before = Instant.now();
    ProductUserEntity result = mapper.toEntity(relation);
    Instant after = Instant.now();

    assertNotNull(result);
    assertEquals(RELATION_ID, result.getProductUserId());
    assertEquals(PRODUCT_ID, result.getProductId());
    assertEquals(USER_ID, result.getUserId());

    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    assertFalse(result.getCreatedAt().isBefore(before));
    assertFalse(result.getCreatedAt().isAfter(after));
    assertFalse(result.getUpdatedAt().isBefore(before));
    assertFalse(result.getUpdatedAt().isAfter(after));
    assertFalse(result.getCreatedAt().isAfter(result.getUpdatedAt()));

    assertNull(result.getVersion());
    assertNull(result.getDeletedAt());
    assertNull(result.getReviews());
  }

  @Test
  void toEntity_withNullRelation_shouldReturnNull() {
    assertNull(mapper.toEntity(null));
  }

  @Test
  void toNewEntity_withValidRelation_shouldMapIdsAndReuseSingleTimestamp() {
    ProductUserRelation relation = relation(RELATION_ID, PRODUCT_ID, USER_ID);

    Instant before = Instant.now();
    ProductUserEntity result = mapper.toNewEntity(relation);
    Instant after = Instant.now();

    assertNotNull(result);
    assertEquals(RELATION_ID, result.getProductUserId());
    assertEquals(PRODUCT_ID, result.getProductId());
    assertEquals(USER_ID, result.getUserId());

    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    assertFalse(result.getCreatedAt().isBefore(before));
    assertFalse(result.getCreatedAt().isAfter(after));
    assertFalse(result.getUpdatedAt().isBefore(before));
    assertFalse(result.getUpdatedAt().isAfter(after));
    assertEquals(result.getCreatedAt(), result.getUpdatedAt());

    assertNull(result.getDeletedAt());
    assertNull(result.getVersion());
  }

  @Test
  void toNewEntity_withNullRelation_shouldReturnNull() {
    assertNull(mapper.toNewEntity(null));
  }

  @Test
  void updateEntity_withValidRelation_shouldUpdateOwnedFieldsOnly() {
    List<ReviewEntity> reviews = new ArrayList<>(List.of(reviewEntity()));
    ProductUserEntity existing = ProductUserEntity.builder()
        .productUserId(RELATION_ID)
        .version(7L)
        .productId(PRODUCT_ID)
        .userId(USER_ID)
        .reviews(reviews)
        .createdAt(CREATED_AT)
        .updatedAt(PREVIOUS_UPDATED_AT)
        .deletedAt(DELETED_AT)
        .build();

    ProductUserRelation relation = relation(OTHER_RELATION_ID, OTHER_PRODUCT_ID, OTHER_USER_ID);

    Instant before = Instant.now();
    ProductUserEntity result = mapper.updateEntity(existing, relation);
    Instant after = Instant.now();

    assertSame(existing, result);
    assertEquals(RELATION_ID, result.getProductUserId());
    assertEquals(OTHER_PRODUCT_ID, result.getProductId());
    assertEquals(OTHER_USER_ID, result.getUserId());

    assertNotNull(result.getUpdatedAt());
    assertFalse(result.getUpdatedAt().isBefore(before));
    assertFalse(result.getUpdatedAt().isAfter(after));

    assertEquals(7L, result.getVersion().longValue());
    assertEquals(CREATED_AT, result.getCreatedAt());
    assertEquals(DELETED_AT, result.getDeletedAt());
    assertSame(reviews, result.getReviews());
  }

  @Test
  void updateEntity_withNullExistingEntity_shouldReturnNull() {
    ProductUserRelation relation = relation(OTHER_RELATION_ID, OTHER_PRODUCT_ID, OTHER_USER_ID);

    assertNull(mapper.updateEntity(null, relation));
  }

  @Test
  void updateEntity_withNullRelation_shouldReturnUnchangedEntity() {
    List<ReviewEntity> reviews = new ArrayList<>(List.of(reviewEntity()));
    ProductUserEntity existing = ProductUserEntity.builder()
        .productUserId(RELATION_ID)
        .version(7L)
        .productId(PRODUCT_ID)
        .userId(USER_ID)
        .reviews(reviews)
        .createdAt(CREATED_AT)
        .updatedAt(PREVIOUS_UPDATED_AT)
        .deletedAt(DELETED_AT)
        .build();

    UUID productUserIdBefore = existing.getProductUserId();
    Long versionBefore = existing.getVersion();
    UUID productIdBefore = existing.getProductId();
    UUID userIdBefore = existing.getUserId();
    Instant createdAtBefore = existing.getCreatedAt();
    Instant updatedAtBefore = existing.getUpdatedAt();
    Instant deletedAtBefore = existing.getDeletedAt();
    List<ReviewEntity> reviewsBefore = existing.getReviews();

    ProductUserEntity result = mapper.updateEntity(existing, null);

    assertSame(existing, result);
    assertEquals(productUserIdBefore, result.getProductUserId());
    assertEquals(versionBefore, result.getVersion());
    assertEquals(productIdBefore, result.getProductId());
    assertEquals(userIdBefore, result.getUserId());
    assertEquals(createdAtBefore, result.getCreatedAt());
    assertEquals(updatedAtBefore, result.getUpdatedAt());
    assertEquals(deletedAtBefore, result.getDeletedAt());
    assertSame(reviewsBefore, result.getReviews());
  }

  @Test
  void toDomainList_withNullList_shouldReturnEmptyList() {
    assertTrue(mapper.toDomainList(null).isEmpty());
  }

  @Test
  void toDomainList_withEmptyList_shouldReturnEmptyList() {
    assertTrue(mapper.toDomainList(List.of()).isEmpty());
  }

  @Test
  void toDomainList_withMultipleEntities_shouldPreserveOrderAndMapIdsIndependently() {
    ProductUserEntity first = entity(RELATION_ID, PRODUCT_ID, USER_ID);
    ProductUserEntity second = entity(OTHER_RELATION_ID, OTHER_PRODUCT_ID, OTHER_USER_ID);

    List<ProductUserRelation> results = mapper.toDomainList(List.of(first, second));

    assertEquals(2, results.size());
    assertEquals(RELATION_ID, results.getFirst().getId().getValue());
    assertEquals(PRODUCT_ID, results.getFirst().getProductId().getValue());
    assertEquals(USER_ID, results.getFirst().getUserId().getValue());
    assertEquals(OTHER_RELATION_ID, results.get(1).getId().getValue());
    assertEquals(OTHER_PRODUCT_ID, results.get(1).getProductId().getValue());
    assertEquals(OTHER_USER_ID, results.get(1).getUserId().getValue());
  }

  @Test
  void toDomainList_withNullElement_shouldPreserveNullElement() {
    List<ProductUserEntity> entities = Arrays.asList(
        entity(RELATION_ID, PRODUCT_ID, USER_ID),
        null,
        entity(OTHER_RELATION_ID, OTHER_PRODUCT_ID, OTHER_USER_ID));

    List<ProductUserRelation> results = mapper.toDomainList(entities);

    assertEquals(3, results.size());
    assertNotNull(results.getFirst());
    assertNull(results.get(1));
    assertNotNull(results.get(2));
    assertEquals(OTHER_RELATION_ID, results.get(2).getId().getValue());
  }

  @Test
  void toEntityList_withNullList_shouldReturnEmptyList() {
    assertTrue(mapper.toEntityList(null).isEmpty());
  }

  @Test
  void toEntityList_withEmptyList_shouldReturnEmptyList() {
    assertTrue(mapper.toEntityList(List.of()).isEmpty());
  }

  @Test
  void toEntityList_withMultipleRelations_shouldPreserveOrderAndMapIdsIndependently() {
    ProductUserRelation first = relation(RELATION_ID, PRODUCT_ID, USER_ID);
    ProductUserRelation second = relation(OTHER_RELATION_ID, OTHER_PRODUCT_ID, OTHER_USER_ID);

    Instant before = Instant.now();
    List<ProductUserEntity> results = mapper.toEntityList(List.of(first, second));
    Instant after = Instant.now();

    assertEquals(2, results.size());
    assertEquals(RELATION_ID, results.getFirst().getProductUserId());
    assertEquals(PRODUCT_ID, results.getFirst().getProductId());
    assertEquals(USER_ID, results.getFirst().getUserId());
    assertEquals(OTHER_RELATION_ID, results.get(1).getProductUserId());
    assertEquals(OTHER_PRODUCT_ID, results.get(1).getProductId());
    assertEquals(OTHER_USER_ID, results.get(1).getUserId());

    for (ProductUserEntity result : results) {
      assertNotNull(result.getCreatedAt());
      assertNotNull(result.getUpdatedAt());
      assertFalse(result.getCreatedAt().isBefore(before));
      assertFalse(result.getCreatedAt().isAfter(after));
      assertFalse(result.getUpdatedAt().isBefore(before));
      assertFalse(result.getUpdatedAt().isAfter(after));
    }
  }

  @Test
  void toEntityList_withNullElement_shouldPreserveNullElement() {
    List<ProductUserRelation> relations = Arrays.asList(
        relation(RELATION_ID, PRODUCT_ID, USER_ID),
        null);

    List<ProductUserEntity> results = mapper.toEntityList(relations);

    assertEquals(2, results.size());
    assertNotNull(results.getFirst());
    assertNull(results.get(1));
  }

  private static ProductUserEntity entity(UUID relationId, UUID productId, UUID userId) {
    return ProductUserEntity.builder()
        .productUserId(relationId)
        .productId(productId)
        .userId(userId)
        .build();
  }

  private static ProductUserRelation relation(UUID relationId, UUID productId, UUID userId) {
    return ProductUserRelation.reconstruct(
        new EntityId(relationId),
        new EntityId(productId),
        new EntityId(userId));
  }

  private static ReviewEntity reviewEntity() {
    return ReviewEntity.builder()
        .reviewId(REVIEW_ID)
        .productUserId(RELATION_ID)
        .rating(5)
        .comment("Great product")
        .build();
  }
}
