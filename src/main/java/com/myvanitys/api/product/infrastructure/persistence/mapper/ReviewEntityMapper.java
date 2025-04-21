package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ReviewEntityMapper {

  public ProductUserRelation toProductUserRelation(ReviewEntity reviewEntity) {
    // Check if reviewId exists. If it does not exist, we do not map the relationship.
    if (reviewEntity.getReviewId() == null) {
      return null;
    }

    return new ProductUserRelation(
        new EntityId(UUID.randomUUID()),  // This is the ID of the relationship, not of the review
        new EntityId(reviewEntity.getProductUserEntity().getProductId()), // productId
        new EntityId(reviewEntity.getProductUserEntity().getUserId()),    // userId
        new EntityId(reviewEntity.getReviewId())  // ReviewId of the existing entity
    );
  }

  // Inverse method to create ReviewEntity from ProductUserRelation
  public ReviewEntity toReviewEntity(ProductUserRelation productUserRelation, int rating, String comment) {
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductId(productUserRelation.getProductId().getValue());
    productUserEntity.setUserId(productUserRelation.getUserId().getValue());

    return ReviewEntity.builder()
        .productUserEntity(productUserEntity)
        .rating(rating)
        .comment(comment)
        .reviewId(UUID.randomUUID())
        .build();
  }
}
