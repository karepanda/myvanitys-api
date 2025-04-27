package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;


import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between domain Review and JPA ReviewEntity.
 */
@Component
@AllArgsConstructor
public class ReviewMapper {

  private final JpaProductUserRepository productUserRepository;


  /**
   * Converts a ReviewEntity to a domain Review object.
   *
   * @param entity The entity to convert
   * @param  productUserId associated with the review
   * @return The domain Review object
   */
  public Review toDomain(ReviewEntity entity, EntityId productUserId) {
    if (entity == null) {
      return null;
    }

    // Ensure the product is not null
    if (productUserId == null) {
      throw new IllegalArgumentException("Product cannot be null for review conversion");
    }

    // Obtener el ProductUserEntity usando el productUserId
    ProductUserEntity productUserEntity = productUserRepository
            .findById(entity.getProductUserId())
            .orElseThrow(() -> new EntityNotFoundException(
                    "ProductUser relation not found for review: " + entity.getReviewId()));


    // Extract necessary IDs
    EntityId reviewId = new EntityId(entity.getReviewId());
    EntityId userId = new EntityId(productUserEntity.getUserId());

    // Build and return the domain object
    return new Review(
        reviewId,
        userId,
        productUserId,
        entity.getRating(),
        entity.getComment()
    );
  }

  /**
   * Converts a domain Review object to a ReviewEntity.
   *
   * @param domain The domain object to convert
   * @return The corresponding ReviewEntity
   */
  public ReviewEntity toEntity(Review domain) {
    if (domain == null) {
      return null;
    }

    // Extract the UUID of the review
    UUID reviewId = domain.getId() != null ? domain.getId().getValue() : null;

    // Build the entity
    return ReviewEntity.builder()
        .reviewId(reviewId)
        .rating(domain.getRating())
        .comment(domain.getComment())
        // The productUserEntity relation is not set here
        // since it is handled in the adapter layer
        .build();
  }
}
