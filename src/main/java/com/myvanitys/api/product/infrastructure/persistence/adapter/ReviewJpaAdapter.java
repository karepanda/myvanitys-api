package com.myvanitys.api.product.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.application.port.output.ReviewRepositoryPort;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ReviewRepository;
import org.springframework.stereotype.Component;

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
  public Review save(Review review) {
    return null;
  }

  @Override
  public Optional<Review> findById(EntityId id) {
    return Optional.empty();
  }

  @Override
  public List<Review> findByProductId(EntityId productId) {
    return List.of();
  }

  @Override
  public List<Review> findByUserId(EntityId userId) {
    return List.of();
  }

}