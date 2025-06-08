package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ReviewEntityMapper {

  /**
   * Converts a ProductUserEntity to a domain ProductUserRelation object.
   */
  public ProductUserEntity toProductUserEntity(ProductUserRelation relation) {
    if (relation == null) {
      return null;
    }

    ProductUserEntity entity = new ProductUserEntity();
    entity.setProductUserId(relation.getId().getValue());
    entity.setProductId(relation.getProductId().getValue());
    entity.setUserId(relation.getUserId().getValue());
    return entity;
  }

  public ReviewEntity createReviewEntity(EntityId productUserId, int rating, String comment) {
    return ReviewEntity.builder()
        .reviewId(UUID.randomUUID())
        .rating(rating)
        .comment(comment)
        .productUserId(productUserId.getValue())
        .build();
  }

  public ProductUserRelation toProductUserRelation(ProductUserEntity entity) {
    if (entity == null) {
      return null;
    }

    return ProductUserRelation.reconstruct(
        new EntityId(entity.getProductUserId()),
        new EntityId(entity.getProductId()),
        new EntityId(entity.getUserId())
    );
  }
}