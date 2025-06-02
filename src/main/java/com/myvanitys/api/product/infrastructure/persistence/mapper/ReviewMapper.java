package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;


@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface ReviewMapper {

  /**
   * Converts a ReviewEntity to a domain Review object. This is implemented manually due to the factory method requirements.
   *
   * @param entity The entity to convert
   * @return The domain Review object
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
   *
   * @param review The domain object to convert
   * @return The corresponding ReviewEntity
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
   *
   * @param review The domain object to convert
   * @param productUserId The UUID of the product-user relation
   * @return The corresponding ReviewEntity
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