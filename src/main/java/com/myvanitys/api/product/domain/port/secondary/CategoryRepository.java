package com.myvanitys.api.product.domain.port.secondary;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;

/**
 * Secondary port for Category repository operations This interface is implemented by adapters in the infrastructure layer
 */
public interface CategoryRepository {

  /**
   * Find a category by its ID
   *
   * @param categoryId the category ID
   * @return the category if found
   */
  Optional<Category> findById(EntityId categoryId);

  /**
   * Find a category by its name
   *
   * @param name the category name
   * @return the category if found
   */
  Optional<Category> findByName(String name);

  /**
   * Get all categories
   *
   * @return list of all categories
   */
  List<Category> findAll();

  /**
   * Save a category
   *
   * @param category the category to save
   * @return the saved category
   */
  Category save(Category category);

  /**
   * Delete a category by its ID
   *
   * @param categoryId the category ID to delete
   */
  void deleteById(EntityId categoryId);
}