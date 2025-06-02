package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

  /**
   * Finds reviews by product ID through the product user relationship
   */
  List<ReviewEntity> findByProductUserId(UUID productUserId);
}