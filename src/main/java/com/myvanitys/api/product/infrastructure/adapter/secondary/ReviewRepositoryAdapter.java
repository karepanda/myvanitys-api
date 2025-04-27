package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementation of the ReviewRepository port from the domain. This adapter connects the domain with the JPA persistence
 * infrastructure.
 */
@Component
@AllArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

  private final JpaReviewRepository jpaReviewRepository;

  private final JpaProductUserRepository jpaProductUserRepository;

  private final JpaProductRepository jpaProductRepository;

  private final ReviewMapper reviewMapper;

  private final ProductMapper productMapper;

  @Override
  @Transactional
  public Review save(Review review) {
    try {

      // Convert review to entity
      ReviewEntity entity = reviewMapper.toEntity(review);

      // Set timestamps
      Instant now = Instant.now();
      if (entity.getCreatedAt() == null) {
        entity.setCreatedAt(now);
      }
      entity.setUpdatedAt(now);

      // Assign product-user relationship to review entity
      entity.setProductUserId(UUID.randomUUID());

      // Save entity
      ReviewEntity savedEntity = jpaReviewRepository.save(entity);

      // Retrieve product from domain
      EntityId productUserEntity = review.getProductUserId();

      // Convert back to domain using the existing product
      return reviewMapper.toDomain(savedEntity, productUserEntity);
    } catch (DataAccessException e) {
      throw new DatabaseException("Error saving review", e);
    }
  }

  @Override
  public Optional<Review> findById(EntityId reviewId) {
    try {
      UUID uuid = reviewId.getValue();
      Optional<ReviewEntity> reviewEntityOpt = jpaReviewRepository.findById(uuid);

      if (reviewEntityOpt.isEmpty()) {
        return Optional.empty();
      }

      ReviewEntity reviewEntity = reviewEntityOpt.get();
      UUID productId = reviewEntity.getProductUserEntity().getProductId();

      // Get the product
      return jpaProductRepository.findById(productId)
          .map(productEntity -> {
            // Convert product entity to domain
            EntityId productUserEntity = new EntityId(reviewEntity.getProductUserEntity().getProductUserId());
            return reviewMapper.toDomain(reviewEntity, productUserEntity);
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
      List<ReviewEntity> reviewEntities = jpaReviewRepository.findByProductUserEntityProductId(uuid);

      if (reviewEntities.isEmpty()) {
        return List.of();
      }

      // Retrieve product once
      Optional<Product> productOpt = jpaProductRepository.findById(uuid)
          .map(productMapper::toDomain);

      if (productOpt.isEmpty()) {
        throw new EntityNotFoundException("Product not found with id: " + uuid);
      }

      Product product = productOpt.get();
      EntityId productUserEntity = new EntityId(product.getId().getValue());

      // Convert all reviews
      return reviewEntities.stream()
          .map(entity -> reviewMapper.toDomain(entity, productUserEntity))
          .toList();
    } catch (DataAccessException e) {
      throw new DatabaseException("Error finding reviews by product", e);
    }
  }

  @Override
  public List<Review> findByUserId(EntityId userId) {
    try {
      UUID uuid = userId.getValue();
      List<ReviewEntity> reviewEntities = jpaReviewRepository.findByProductUserEntityUserId(uuid);

      if (reviewEntities.isEmpty()) {
        return List.of();
      }

      // For each review, fetch the corresponding product
      return reviewEntities.stream()
          .map(entity -> {
            UUID productId = entity.getProductUserEntity().getProductId();
            return jpaProductRepository.findById(productId)
                .map(productEntity -> {
                  EntityId productUserEntity = new EntityId(entity.getProductUserEntity().getProductUserId());
                  return reviewMapper.toDomain(entity, productUserEntity);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                    "Product not found for review: " + entity.getReviewId()));
          })
          .toList();
    } catch (DataAccessException e) {
      throw new DatabaseException("Error finding reviews by user", e);
    }
  }

  @Override
  public boolean existsByReviewIdAndUserId(EntityId reviewId, EntityId userId) {
    try {
      UUID reviewUuid = reviewId.getValue();
      UUID userUuid = userId.getValue();
      return jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewUuid, userUuid);
    } catch (DataAccessException e) {
      throw new DatabaseException("Error checking if review exists", e);
    }
  }
}
