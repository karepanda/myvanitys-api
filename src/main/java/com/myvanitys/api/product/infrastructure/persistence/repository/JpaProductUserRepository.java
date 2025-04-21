package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for ProductUserEntity This interface is used by the ProductUserRepositoryAdapter to access the database
 */
@Repository
public interface JpaProductUserRepository extends JpaRepository<ProductUserEntity, UUID> {

  /**
   * Find product-user relationships by product ID
   */
  List<ProductUserEntity> findByProductId(UUID productId);

  /**
   * Find product-user relationships by user ID
   */
  List<ProductUserEntity> findByUserId(UUID userId);

  /**
   * Find a specific relationship between a product and a user
   */
  Optional<ProductUserEntity> findByProductIdAndUserId(UUID productId, UUID userId);

  /**
   * Check if a relationship exists between a product and a user
   */
  boolean existsByProductIdAndUserId(UUID productId, UUID userId);

  /**
   * Delete all relationships for a product
   */
  @Modifying
  @Query("DELETE FROM ProductUserEntity pu WHERE pu.productId = :productId")
  void deleteByProductId(@Param("productId") UUID productId);
}