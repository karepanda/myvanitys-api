package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface ReviewMapper {

  /**
   * Converts a ReviewEntity to a domain Review object.
   */
  default Review toDomain(ReviewEntity entity) {
    if (entity == null) {
      return null;
    }

    ReviewDetails details = ReviewDetails.of(
        entity.getRating(),
        entity.getComment(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt()
    );

    return Review.createWithExistingId(
        new EntityId(entity.getReviewId()),
        EntityId.of(entity.getProductUserId()),
        details
    );
  }

  /**
   * Converts a domain Review object to a ReviewEntity.
   */
  @Mapping(source = "id", target = "reviewId")
  @Mapping(source = "productUserId", target = "productUserId")
  @Mapping(source = "details.rating", target = "rating")
  @Mapping(source = "details.comment", target = "comment")
  @Mapping(expression = "java(review.getCreatedAt())", target = "createdAt")
  @Mapping(expression = "java(review.getUpdatedAt())", target = "updatedAt")
  @Mapping(expression = "java(review.getDeletedAt())", target = "deletedAt")
  ReviewEntity toEntity(Review review);

  /**
   * Converts a domain Review object to a ReviewEntity with explicit product-user ID.
   */
  @Mapping(source = "review.id", target = "reviewId")
  @Mapping(source = "productUserId", target = "productUserId")
  @Mapping(source = "review.details.rating", target = "rating")
  @Mapping(source = "review.details.comment", target = "comment")
  @Mapping(expression = "java(review.getCreatedAt())", target = "createdAt")
  @Mapping(expression = "java(review.getUpdatedAt())", target = "updatedAt")
  @Mapping(expression = "java(review.getDeletedAt())", target = "deletedAt")
  ReviewEntity toEntity(Review review, UUID productUserId);
}