package com.myvanitys.api.product.domain.port.secondary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;


public interface ProductUserRepository {

  /**
   * Save a relationship between a product and a user
   *
   * @param productId the product ID
   * @param userId the user ID
   */
  void saveProductUserRelationship(EntityId productId, EntityId userId);

  /**
   * Check if a user is associated with a product
   *
   * @param productId the product ID
   * @param userId the user ID
   * @return true if the user is associated with the product
   */
  boolean isUserAssociatedWithProduct(EntityId productId, EntityId userId);

  /**
   * Delete all relationships for a product
   *
   * @param productId the product ID
   */
  void deleteByProductId(EntityId productId);

  /**
   * Check if a product is associated with a user
   *
   * @param productId the product ID
   * @param userId the user ID
   * @return true if the product is associated with the user
   */
  boolean existsByProductIdAndUserId(EntityId productId, EntityId userId);

  /**
   * Find all product-user relationships for a user
   *
   * @param userId the user ID
   * @return list of relationship objects containing product IDs
   */
  List<EntityId> findProductIdsByUserId(EntityId userId);

  /**
   * Find product-user relationships by product ID
   *
   * @param productId the product ID
   * @return list of product-user relationships
   */
  List<ProductUserEntity> findByProductId(UUID productId);

  /**
   * Find product-user relationships by user ID
   *
   * @param userId the user ID
   * @return list of product-user relationships
   */
  List<ProductUserEntity> findByUserId(UUID userId);

  /**
   * Find a specific relationship between a product and a user
   *
   * @param productId the product ID
   * @param userId the user ID
   * @return the product-user relationship if found
   */
  Optional<ProductUserEntity> findByProductIdAndUserId(UUID productId, UUID userId);
}