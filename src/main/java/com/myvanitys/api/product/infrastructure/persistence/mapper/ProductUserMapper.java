package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ProductUserMapper {

  @Mapping(target = "id", source = "productUserId")
  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "reviewId", ignore = true)
  public abstract ProductUserRelation toDomain(ProductUserEntity productUserEntity);

  @Mapping(target = "productUserId", source = "id.value")
  @Mapping(target = "userId", source = "userId.value")
  @Mapping(target = "productId", source = "productId.value")
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract ProductUserEntity toEntity(ProductUserRelation productUserRelation);

  @AfterMapping
  protected void setReviewId(@MappingTarget ProductUserRelation relation, ProductUserEntity entity) {
    if (entity.getReviews() != null && !entity.getReviews().isEmpty()) {
      // Take the ID of the first review if it exists
      ReviewEntity firstReview = entity.getReviews().getFirst();
      if (firstReview.getReviewId() != null) {
        relation.setReviewId(new EntityId(firstReview.getReviewId()));
      }
    }
  }
}