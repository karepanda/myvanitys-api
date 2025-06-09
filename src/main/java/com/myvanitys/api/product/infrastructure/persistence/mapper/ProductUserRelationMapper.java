package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.time.Instant;
import java.util.List;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductUserRelationMapper {

  /**
   * Maps ProductUserEntity to ProductUserRelation domain object
   */
  default ProductUserRelation toDomain(ProductUserEntity entity) {
    if (entity == null) {
      return null;
    }

    EntityId id = new EntityId(entity.getProductUserId());
    EntityId productId = new EntityId(entity.getProductId());
    EntityId userId = new EntityId(entity.getUserId());

    return ProductUserRelation.reconstruct(id, productId, userId);
  }

  /**
   * Maps ProductUserRelation domain object to ProductUserEntity
   */
  default ProductUserEntity toEntity(ProductUserRelation relation) {
    if (relation == null) {
      return null;
    }

    return ProductUserEntity.builder()
        .productUserId(relation.getId().getValue())
        .productId(relation.getProductId().getValue())
        .userId(relation.getUserId().getValue())
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * Maps list of ProductUserEntity to list of ProductUserRelation
   */
  default List<ProductUserRelation> toDomainList(List<ProductUserEntity> entities) {
    if (entities == null) {
      return List.of();
    }

    return entities.stream()
        .map(this::toDomain)
        .toList();
  }

  /**
   * Maps list of ProductUserRelation to list of ProductUserEntity
   */
  default List<ProductUserEntity> toEntityList(List<ProductUserRelation> relations) {
    if (relations == null) {
      return List.of();
    }

    return relations.stream()
        .map(this::toEntity)
        .toList();
  }

  /**
   * Updates an existing entity with domain values
   */
  default ProductUserEntity updateEntity(ProductUserEntity existingEntity, ProductUserRelation relation) {
    if (existingEntity == null || relation == null) {
      return existingEntity;
    }

    existingEntity.setProductId(relation.getProductId().getValue());
    existingEntity.setUserId(relation.getUserId().getValue());
    existingEntity.setUpdatedAt(Instant.now());

    return existingEntity;
  }

  /**
   * Creates a new entity for persistence with auto-generated timestamps
   */
  default ProductUserEntity toNewEntity(ProductUserRelation relation) {
    if (relation == null) {
      return null;
    }

    Instant now = Instant.now();
    return ProductUserEntity.builder()
        .productUserId(relation.getId().getValue())
        .productId(relation.getProductId().getValue())
        .userId(relation.getUserId().getValue())
        .createdAt(now)
        .updatedAt(now)
        .deletedAt(null)
        .build();
  }
}