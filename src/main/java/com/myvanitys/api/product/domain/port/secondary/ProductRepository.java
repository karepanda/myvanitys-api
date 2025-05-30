package com.myvanitys.api.product.domain.port.secondary;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port for Product repository operations This interface is implemented by adapters in the infrastructure layer
 */
public interface ProductRepository {

  /**
   * Save a product
   *
   * @param product the product to save
   * @return the saved product
   */
  Product save(Product product);

  /**
   * Find a product by its ID
   *
   * @param productId the product ID
   * @return the product if found
   */
  Optional<Product> findById(EntityId productId);

  /**
   * Find a product by its name
   *
   * @param productName the product name
   * @return the product if found
   */
  Optional<Product> findByName(String productName, EntityId entityId);

  /**
   * Delete a product by its ID
   *
   * @param productId the product ID to delete
   */
  void deleteById(EntityId productId);

  /**
   * Find all products associated with a user
   *
   * @param userId the user ID
   * @return list of products belonging to the user
   */
  List<Product> findByUserId(UUID userId);

  /**
   * Find all products
   *
   * @return list of all products
   */
  List<Product> findAll();
}