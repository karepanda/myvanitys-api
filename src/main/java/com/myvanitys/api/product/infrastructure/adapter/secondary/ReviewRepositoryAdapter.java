package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementation of the ReviewRepository port from the domain This adapter connects the domain with the JPA persistence
 * infrastructure
 */
@Component
public class ReviewRepositoryAdapter implements ReviewRepository {

  private final JpaReviewRepository jpaReviewRepository;

  private final JpaProductUserRepository jpaProductUserRepository;

  private final ReviewMapper reviewMapper;

  @Autowired
  public ReviewRepositoryAdapter(
      JpaReviewRepository jpaReviewRepository,
      JpaProductUserRepository jpaProductUserRepository,
      ReviewMapper reviewMapper) {
    this.jpaReviewRepository = jpaReviewRepository;
    this.jpaProductUserRepository = jpaProductUserRepository;
    this.reviewMapper = reviewMapper;
  }

  @Override
  @Transactional
  public Review save(Review review) {
    // Buscar la relación producto-usuario existente
    ProductUserEntity productUserEntity = jpaProductUserRepository.findByProductIdAndUserId(
        review.getProduct().getId().getValue(),
        review.getUserId().getValue());

    // Convertir la review a entidad
    ReviewEntity entity = reviewMapper.toEntity(review);

    // Asignar la relación producto-usuario a la entidad review
    entity.setProductUserEntity(productUserEntity);

    // Guardar la entidad
    ReviewEntity savedEntity = jpaReviewRepository.save(entity);

    // Convertir de vuelta al dominio
    return reviewMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Review> findById(EntityId reviewId) {
    UUID uuid = reviewId.getValue();
    return jpaReviewRepository.findById(uuid)
        .map(reviewMapper::toDomain);
  }

  @Override
  @Transactional
  public void deleteById(EntityId reviewId) {
    UUID uuid = reviewId.getValue();
    jpaReviewRepository.deleteById(uuid);
  }

  @Override
  public List<Review> findByProductId(EntityId productId) {
    UUID uuid = productId.getValue();
    return jpaReviewRepository.findByProductUserEntityProductId(uuid).stream()
        .map(reviewMapper::toDomain)
        .toList();
  }

  @Override
  public List<Review> findByUserId(EntityId userId) {
    UUID uuid = userId.getValue();
    return jpaReviewRepository.findByProductUserEntityUserId(uuid).stream()
        .map(reviewMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByReviewIdAndUserId(EntityId reviewId, EntityId userId) {
    UUID reviewUuid = reviewId.getValue();
    UUID userUuid = userId.getValue();
    return jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewUuid, userUuid);
  }
}