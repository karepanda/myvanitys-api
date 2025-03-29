package com.myvanitys.api.product.infrastructure.adapter.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.myvanitys.api.product.application.port.output.ReviewRepositoryPort;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ReviewRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReviewJpaAdapter implements ReviewRepositoryPort {

  private final ReviewRepository reviewRepository;

  private final ProductUserRepository productUserRepository;

  private final ReviewMapper reviewMapper;

  private final ProductUserMapper productUserMapper;

  public ReviewJpaAdapter(
      ReviewRepository reviewRepository,
      ProductUserRepository productUserRepository,
      ReviewMapper reviewMapper,
      ProductUserMapper productUserMapper) {
    this.reviewRepository = reviewRepository;
    this.productUserRepository = productUserRepository;
    this.reviewMapper = reviewMapper;
    this.productUserMapper = productUserMapper;
  }

  @Override
  @Transactional
  public Review save(Review review) {
    // Find the ProductUserEntity associated with this review
    UUID userId = review.getUserId().getValue();
    UUID productId = review.getProduct().getId().getValue();
    ProductUserEntity productUserEntity = productUserRepository.findByProductIdAndUserId(productId, userId);

    if (productUserEntity == null) {
      throw new IllegalArgumentException("Product-user relationship not found");
    }

    // Convert review domain object to entity
    ReviewEntity reviewEntity = reviewMapper.toEntity(review);

    // Save entity and convert back to domain object
    ReviewEntity savedEntity = reviewRepository.save(reviewEntity);
    return reviewMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Review> findById(EntityId id) {
    return reviewRepository
        .findById(id.getValue())
        .map(reviewMapper::toDomain);
  }

  @Override
  public List<Review> findByProductId(EntityId productId) {
    return reviewRepository
        .findByProductUserEntityProductId(productId.getValue())
        .stream()
        .map(reviewMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Review> findByUserId(EntityId userId) {
    return reviewRepository
        .findByProductUserEntityUserId(userId.getValue())
        .stream()
        .map(reviewMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteById(EntityId id) {
    reviewRepository.deleteById(id.getValue());
  }
}