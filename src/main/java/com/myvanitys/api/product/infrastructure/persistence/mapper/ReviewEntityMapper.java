package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ReviewEntityMapper {

  public ProductUserRelation toProductUserRelation(ProductUserRelation productUserRelation) {
    if (productUserRelation.getReviewId() == null) {
      return null;
    }
    
    return ProductUserRelation.reconstruct(
        new EntityId(productUserRelation.getId().getValue()),
        new EntityId(productUserRelation.getProductId().getValue()),
        new EntityId(productUserRelation.getUserId().getValue()),
        new EntityId(productUserRelation.getReviewId().getValue())
    );
  }

  // Inverse method to create ReviewEntity from ProductUserRelation
  public ReviewEntity toReviewEntity(ProductUserRelation productUserRelation, int rating, String comment) {
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserRelation.getId().getValue());
    productUserEntity.setProductId(productUserRelation.getProductId().getValue());
    productUserEntity.setUserId(productUserRelation.getUserId().getValue());

    return ReviewEntity.builder()
        .reviewId(UUID.randomUUID())
        .rating(rating)
        .comment(comment)
        .productUserId(productUserEntity.getProductUserId())
        .build();
  }
}