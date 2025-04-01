package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for CategoryEntity This interface is used by the CategoryRepositoryAdapter to access the database
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

  /**
   * Find a category by its name
   *
   * @param name the category name
   * @return the category entity if found
   */
  Optional<CategoryEntity> findByName(String name);
}