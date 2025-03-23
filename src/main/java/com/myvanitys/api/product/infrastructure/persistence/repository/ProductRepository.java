package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for product entities
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

  /**
   * Finds products by user ID using JPQL
   */
  @Query("SELECT p FROM ProductEntity p JOIN ProductUserEntity pu ON p.productId = pu.productId WHERE pu.userId = :userId")
  List<ProductEntity> findByUserId(@Param("userId") UUID userId);

  /**
   * Finds product by name
   */
  Optional<ProductEntity> findByName(String name);

  /**
   * Finds product by brand
   */
  Optional<ProductEntity> findByBrand(String brand);

  /**
   * Finds products by category
   */
  List<ProductEntity> findByCategoryCategoryId(UUID categoryId);

  /**
   * Finds products by category name
   */
  List<ProductEntity> findByCategoryName(String categoryName);

  /**
   * Find product by name and brand
   */
  Optional<ProductEntity> findByNameAndBrand(String name, String brand);
}