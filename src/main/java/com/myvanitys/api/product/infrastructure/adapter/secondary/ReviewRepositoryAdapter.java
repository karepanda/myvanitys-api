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
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
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
 * Adapter implementation of the ReviewRepository port from the domain This adapter connects the domain with the JPA persistence
 * infrastructure
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
      // Obtener IDs
      UUID productId = review.getProduct().getId().getValue();
      UUID userId = review.getUserId().getValue();

      // Buscar la relación producto-usuario existente
      Optional<ProductUserEntity> productUserEntityOpt = jpaProductUserRepository.findByProductIdAndUserId(
          productId, userId);

      // Verificar si existe la relación producto-usuario
      if (productUserEntityOpt.isEmpty()) {
        throw new EntityNotFoundException("Product-User relation not found for product id: "
            + productId + " and user id: " + userId);
      }

      // Convertir la review a entidad
      ReviewEntity entity = reviewMapper.toEntity(review);

      // Establecer timestamps
      Instant now = Instant.now();
      if (entity.getCreatedAt() == null) {
        entity.setCreatedAt(now);
      }
      entity.setUpdatedAt(now);

      // Asignar la relación producto-usuario a la entidad review
      entity.setProductUserEntity(productUserEntityOpt.get());

      // Guardar la entidad
      ReviewEntity savedEntity = jpaReviewRepository.save(entity);

      // Recuperar el producto
      Product product = review.getProduct(); // Ya tenemos el producto del dominio

      // Convertir de vuelta al dominio usando el producto existente
      return reviewMapper.toDomain(savedEntity, product);
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

      // Obtener el producto
      return jpaProductRepository.findById(productId)
          .map(productEntity -> {
            Product product = productMapper.toDomain(productEntity);
            return reviewMapper.toDomain(reviewEntity, product);
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

      // Obtener el producto una sola vez
      Optional<Product> productOpt = jpaProductRepository.findById(uuid)
          .map(productMapper::toDomain);

      if (productOpt.isEmpty()) {
        throw new EntityNotFoundException("Product not found with id: " + uuid);
      }

      Product product = productOpt.get();

      // Convertir todas las reviews
      return reviewEntities.stream()
          .map(entity -> reviewMapper.toDomain(entity, product))
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

      // Para cada review, necesitamos buscar el producto correspondiente
      return reviewEntities.stream()
          .map(entity -> {
            UUID productId = entity.getProductUserEntity().getProductId();
            return jpaProductRepository.findById(productId)
                .map(productEntity -> {
                  Product product = productMapper.toDomain(productEntity);
                  return reviewMapper.toDomain(entity, product);
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