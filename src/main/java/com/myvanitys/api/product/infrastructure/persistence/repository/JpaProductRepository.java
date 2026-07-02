package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

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
  List<ProductEntity> findByCategoryId(UUID categoryId);

  /**
   * Finds products whose name or brand contains the term, ignoring case.
   */
  @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :term, '%'))")
  List<ProductEntity> searchByNameOrBrand(@Param("term") String term);

  @Query("SELECT p FROM ProductEntity p JOIN CategoryEntity c ON p.categoryId = c.categoryId WHERE c.name = :categoryName")
  List<ProductEntity> findByCategoryName(@Param("categoryName") String categoryName);

  @Query(
      "SELECT r FROM ReviewEntity r JOIN ProductUserEntity pu ON r.productUserId = pu.productUserId WHERE pu.productId = :productId and "
          + "pu.userId = :userId")
  List<ReviewEntity> findReviewsByProductIdAndUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);

  @Query(
      "SELECT r FROM ReviewEntity r JOIN ProductUserEntity pu ON r.productUserId = pu.productUserId WHERE pu.productId = :productId")
  List<ReviewEntity> findReviewsByProductId(@Param("productId") UUID productId);
}
