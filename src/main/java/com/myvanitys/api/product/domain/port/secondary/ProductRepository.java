package com.myvanitys.api.product.domain.port.secondary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;

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
  Optional<Product> findByName(String productName);

  /**
   * Find products by category name
   *
   * @param categoryName the category name
   * @return list of products in the category
   */
  List<Product> findByCategoryName(String categoryName);

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
}