package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter for the ReviewRepository interface Implements the application's output port for review persistence operations
 */
@Component
@AllArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

  private final JpaReviewRepository jpaReviewRepository;

  private final JpaProductUserRepository jpaProductUserRepository;

  private final ReviewMapper reviewMapper;

  @Override
  @Transactional
  public Review save(Review review) {
    try {
      // First, verify that the product-user relation exists
      final EntityId productUserId = getEntityId(review);

      // Convert review to entity
      ReviewEntity entity = reviewMapper.toEntity(review);

      // Set timestamps if needed
      Instant now = Instant.now();
      if (entity.getCreatedAt() == null) {
        entity.setCreatedAt(now);
      }
      entity.setUpdatedAt(now);

      // Save entity
      ReviewEntity savedEntity = jpaReviewRepository.save(entity);

      // Convert back to domain
      return reviewMapper.toDomain(savedEntity);
    } catch (DataAccessException e) {
      throw new DatabaseException("Error saving review", e);
    }
  }

  private EntityId getEntityId(Review review) {
    EntityId productUserId = review.getProductUserId();
    UUID productUserUUID = productUserId.getValue();

    jpaProductUserRepository.findById(productUserUUID)
        .orElseThrow(() -> new EntityNotFoundException(
            "ProductUser relation not found for review: " + review.getId().getValue()));
    return productUserId;
  }

  @Override
  public Optional<Review> findById(EntityId reviewId) {
    try {
      UUID uuid = reviewId.getValue();
      return jpaReviewRepository.findById(uuid)
          .flatMap(reviewEntity -> {
            // Get the product-user relation
            UUID productUserUUID = reviewEntity.getProductUserId();
            return jpaProductUserRepository.findById(productUserUUID)
                .map(productUserEntity -> {
                  EntityId productUserEntityId = new EntityId(productUserUUID);
                  return reviewMapper.toDomain(reviewEntity);
                });
          });
    } catch (DataAccessException e) {
      throw new DatabaseException("Error finding review", e);
    }
  }

  @Override
  @Transactional
  public void deleteById(EntityId reviewId) {
    try {
      UUID uuid = reviewId.getValue();
      jpaReviewRepository.deleteById(uuid);
    } catch (DataAccessException e) {
      throw new DatabaseException("Error deleting review", e);
    }
  }

  @Override
  public List<Review> findByProductId(EntityId productId) {
    try {
      UUID uuid = productId.getValue();

      // Find all product-user relations for this product
      List<ProductUserEntity> productUserEntities = jpaProductUserRepository.findByProductId(uuid);

      // Map to collect reviews for each product-user relation
      return productUserEntities.stream()
          .flatMap(productUserEntity -> {
            UUID productUserUUID = productUserEntity.getProductUserId();
            EntityId productUserEntityId = new EntityId(productUserUUID);

            // Find reviews for this product-user relation
            return jpaReviewRepository.findByProductUserId(productUserUUID)
                .stream()
                .map(reviewEntity -> reviewMapper.toDomain(reviewEntity));
          })
          .toList();
    } catch (DataAccessException e) {
      throw new DatabaseException("Error finding reviews by product", e);
    }
  }

  @Override
  public List<Review> findByUserId(EntityId userId) {
    try {
      UUID uuid = userId.getValue();

      // Find all product-user relations for this user
      List<ProductUserEntity> productUserEntities = jpaProductUserRepository.findByUserId(uuid);

      // Map to collect reviews for each product-user relation
      return productUserEntities.stream()
          .flatMap(productUserEntity -> {
            UUID productUserUUID = productUserEntity.getProductUserId();
            EntityId productUserEntityId = new EntityId(productUserUUID);

            // Find reviews for this product-user relation
            return jpaReviewRepository.findByProductUserId(productUserUUID)
                .stream()
                .map(reviewEntity -> reviewMapper.toDomain(reviewEntity));
          })
          .toList();
    } catch (DataAccessException e) {
      throw new DatabaseException("Error finding reviews by user", e);
    }
  }

  @Override
  public List<Review> findByProductUserId(EntityId productUserId) {
    return List.of();
  }

  @Override
  public boolean existsByReviewIdAndUserId(EntityId reviewId, EntityId userId) {
    try {
      UUID reviewUuid = reviewId.getValue();
      UUID userUuid = userId.getValue();

      // First find the review
      Optional<ReviewEntity> reviewEntityOpt = jpaReviewRepository.findById(reviewUuid);

      if (reviewEntityOpt.isEmpty()) {
        return false;
      }

      // Get the product-user ID from the review
      UUID productUserUUID = reviewEntityOpt.get().getProductUserId();

      // Check if this product-user relation belongs to the specified user
      return jpaProductUserRepository.findById(productUserUUID)
          .map(productUserEntity -> productUserEntity.getUserId().equals(userUuid))
          .orElse(false);
    } catch (DataAccessException e) {
      throw new DatabaseException("Error checking if review exists", e);
    }
  }
}