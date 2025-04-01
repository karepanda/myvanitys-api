package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for ProductUserEntity This interface is used by the ProductUserRepositoryAdapter to access the database
 */
@Repository
public interface JpaProductUserRepository extends JpaRepository<ProductUserEntity, UUID> {

  /**
   * Find all product-user relationships for a user
   *
   * @param userId the user ID
   * @return list of product-user relationships
   */
  List<ProductUserEntity> findByUserId(UUID userId);

  /**
   * Delete all relationships for a product
   *
   * @param productId the product ID
   */
  void deleteByProductId(UUID productId);

  /**
   * Check if a relationship exists between a product and a user
   *
   * @param productId the product ID
   * @param userId the user ID
   * @return true if the relationship exists
   */
  boolean existsByProductIdAndUserId(UUID productId, UUID userId);

  /**
   * Find a specific product-user relationship
   *
   * @param productId the product ID
   * @param userId the user ID
   * @return the product-user entity if found
   */
  ProductUserEntity findByProductIdAndUserId(UUID productId, UUID userId);
}