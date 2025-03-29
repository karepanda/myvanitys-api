package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

  /**
   * Find a category entity by its ID
   *
   * @param categoryId the category ID
   * @return the category entity if found, otherwise empty
   */
  Optional<CategoryEntity> findByCategoryId(UUID categoryId);

  /**
   * Finds a domain category by its domain ID
   *
   * @param id the domain entity ID
   * @return the domain category if found, otherwise empty
   */
  default Optional<Category> findById(EntityId id) {
    return findByCategoryId(id.getValue())
        .map(entity -> new Category(
            new EntityId(entity.getCategoryId()),
            entity.getName()));
  }

}