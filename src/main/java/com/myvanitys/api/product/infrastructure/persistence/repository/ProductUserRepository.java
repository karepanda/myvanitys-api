package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for product-user relationships
 */
@Repository
public interface ProductUserRepository extends JpaRepository<ProductUserEntity, UUID> {

  /**
   * Finds product-user entities by user ID
   */
  List<ProductUserEntity> findByUserId(UUID userId);

  /**
   * Finds product-user entities by product ID
   */
  List<ProductUserEntity> findByProductId(UUID productId);

  /**
   * Deletes product-user relationships by product ID
   */
  void deleteByProductId(UUID productId);

  /**
   * Deletes product-user relationships by user ID
   */
  void deleteByUserId(UUID userId);

  /**
   * Find by product ID and user ID
   */
  ProductUserEntity findByProductIdAndUserId(UUID productId, UUID userId);

  /**
   * Checks if a relationship exists between product and user
   */
  boolean existsByProductIdAndUserId(UUID productId, UUID userId);
}