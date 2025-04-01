package com.myvanitys.api.product.domain.port.secondary;

import java.util.List;

import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Secondary port for product-user relationship operations
 */
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
}