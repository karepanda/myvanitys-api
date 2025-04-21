package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.InfrastructureException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;

  private final JpaCategoryRepository jpaCategoryRepository;

  private final ProductUserRepository productUserRepository;

  private final ProductMapper productMapper;

  private final CategoryMapper categoryMapper;

  @Override
  public Product save(Product product) {
    try {
      // Check if the category exists before saving the product
      UUID categoryId = product.getCategory().categoryId().getValue();
      if (!jpaCategoryRepository.existsById(categoryId)) {
        throw new RepositoryResourceNotFoundException("Category not found with ID: " + categoryId);
      }

      // Convert product to entity
      ProductEntity entity = productMapper.toEntity(product);

      // Set audit fields if it's a new entity
      if (entity.getCreatedAt() == null) {
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
      } else {
        entity.setUpdatedAt(Instant.now());
      }

      // Save the entity
      ProductEntity savedEntity = jpaProductRepository.save(entity);

      // Return the product with its category
      return productMapper.toDomain(savedEntity, product.getCategory());
    } catch (DataAccessException e) {
      log.error("Error saving product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Save product", e);
    } catch (Exception e) {
      if (!(e instanceof InfrastructureException)) {
        log.error("Unexpected error saving product: {}", e.getMessage(), e);
        throw new DatabaseException("Error saving product", e);
      }
      throw e;
    }
  }

  @Override
  public Optional<Product> findById(EntityId productId) {
    try {
      UUID uuid = productId.getValue();
      return jpaProductRepository.findById(uuid)
          .map(productEntity -> {
            // Load the associated category
            Category category = getCategoryForProduct(productEntity);
            return productMapper.toDomain(productEntity, category);
          });
    } catch (DataAccessException e) {
      log.error("Error finding product by ID: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find product by ID", e);
    }
  }

  @Override
  public Optional<Product> findByName(String productName) {
    try {
      return jpaProductRepository.findByName(productName)
          .map(productEntity -> {
            // Load the category
            Category category = getCategoryForProduct(productEntity);
            return productMapper.toDomain(productEntity, category);
          });
    } catch (DataAccessException e) {
      log.error("Error finding product by name: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find product by name", e);
    }
  }

  @Override
  public List<Product> findByCategoryName(String categoryName) {
    try {
      return jpaCategoryRepository.findByName(categoryName)
          .map(categoryEntity -> {
            UUID categoryId = categoryEntity.getCategoryId();
            List<ProductEntity> products = jpaProductRepository.findByCategoryId(categoryId);

            // Convert the category to domain object
            Category category = categoryMapper.toDomain(categoryEntity);

            // Map each product with the same category
            return products.stream()
                .map(productEntity -> productMapper.toDomain(productEntity, category))
                .toList();
          })
          .orElse(Collections.emptyList());
    } catch (DataAccessException e) {
      log.error("Error finding products by category name: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find products by category name", e);
    }
  }

  @Override
  public List<Product> findByUserId(UUID userId) {
    try {
      // Get product IDs associated with the user
      List<EntityId> productIds = productUserRepository.findProductIdsByUserId(new EntityId(userId));

      // Return empty list if no products found
      if (productIds.isEmpty()) {
        return Collections.emptyList();
      }

      // Convert EntityId to UUID for query
      List<UUID> productUuids = productIds.stream()
          .map(EntityId::getValue)
          .toList();

      // Fetch product entities
      List<ProductEntity> productEntities = jpaProductRepository.findAllById(productUuids);

      // Convert to domain objects including their categories
      return productEntities.stream()
          .map(productEntity -> {
            // Load the category for each product
            Category category = getCategoryForProduct(productEntity);
            return productMapper.toDomain(productEntity, category);
          })
          .toList();
    } catch (DataAccessException e) {
      log.error("Error finding products by user ID: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find products by user ID", e);
    }
  }

  @Override
  public void deleteById(EntityId productId) {
    try {
      UUID uuid = productId.getValue();
      jpaProductRepository.deleteById(uuid);
      log.debug("Product deleted successfully with ID: {}", productId.getValue());
    } catch (DataAccessException e) {
      log.error("Error deleting product with ID {}: {}", productId.getValue(), e.getMessage(), e);
      throw DatabaseException.queryError("Delete product by ID", e);
    }
  }

  /**
   * Helper method to get a product's category
   */
  private Category getCategoryForProduct(ProductEntity productEntity) {
    try {
      UUID categoryId = productEntity.getCategoryId();
      if (categoryId == null) {
        return null;
      }

      return jpaCategoryRepository.findById(categoryId)
          .map(categoryMapper::toDomain)
          .orElse(null);
    } catch (DataAccessException e) {
      log.error("Error loading category for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find category by ID", e);
    }
  }
}
