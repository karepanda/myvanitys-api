package com.myvanitys.api.product.infrastructure.adapter.secondary;

import com.myvanitys.api.common.InfrastructureException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@AllArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;

  private final JpaCategoryRepository jpaCategoryRepository;

  private final ProductUserRepository productUserRepository;

  private final ProductMapper productMapper;

  private final CategoryMapper categoryMapper;

  private final ReviewMapper reviewMapper;

  @Override
  public Product save(Product product) {
    try {
      UUID categoryId = product.getCategory().categoryId().getValue();
      if (!jpaCategoryRepository.existsById(categoryId)) {
        throw new RepositoryResourceNotFoundException("Category not found with ID: " + categoryId);
      }

      ProductEntity entity = productMapper.toEntity(product);

      if (entity.getCreatedAt() == null) {
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
      } else {
        entity.setUpdatedAt(Instant.now());
      }

      ProductEntity savedEntity = jpaProductRepository.save(entity);

      return productMapper.toDomain(savedEntity, product.getCategory(), product.getReviews());
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
      return jpaProductRepository.findById(productId.getValue())
          .map(productEntity -> {
            Category category = getCategoryForProduct(productEntity);
            List<Review> reviews = getReviewsForProduct(productEntity.getProductId());
            return productMapper.toDomain(productEntity, category, reviews);
          });
    } catch (DataAccessException e) {
      log.error("Error finding product by ID: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find product by ID", e);
    }
  }

  @Override
  public Optional<Product> findByName(String productName, EntityId userId) {
    try {
      return jpaProductRepository.findByName(productName)
          .map(productEntity -> {
            Category category = getCategoryForProduct(productEntity);
            List<Review> reviews = getReviewsUserForProduct(productEntity.getProductId(), userId.getValue());
            return productMapper.toDomain(productEntity, category, reviews);
          });
    } catch (DataAccessException e) {
      log.error("Error finding product by name: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find product by name", e);
    }
  }

  @Override
  public List<Product> findByUserId(UUID userId) {
    try {
      List<EntityId> productIds = productUserRepository.findProductIdsByUserId(new EntityId(userId));

      if (productIds.isEmpty()) {
        return Collections.emptyList();
      }

      List<UUID> productUuids = productIds.stream()
          .map(EntityId::getValue)
          .toList();

      List<ProductEntity> productEntities = jpaProductRepository.findAllById(productUuids);

      return productEntities.stream()
          .map(productEntity -> {
            Category category = getCategoryForProduct(productEntity);
            List<Review> reviews = getReviewsUserForProduct(productEntity.getProductId(), userId);
            return productMapper.toDomain(productEntity, category, reviews);
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

    @Override
    public List<Product> findAll() {
      try {
        List<ProductEntity> productEntities = jpaProductRepository.findAll();

        return productEntities.stream()
                .map(productEntity -> {
                  Category category = getCategoryForProduct(productEntity);
                  List<Review> reviews = getReviewsForProduct(productEntity.getProductId());
                  return productMapper.toDomain(productEntity, category, reviews);
                })
                .filter(Objects::nonNull)
                .toList();
      } catch (DataAccessException e) {
        log.error("Error finding all products: {}", e.getMessage(), e);
        throw DatabaseException.queryError("Find all products", e);
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

  private List<Review> getReviewsUserForProduct(UUID productId, UUID userId) {
    try {
      return jpaProductRepository.findReviewsByProductIdAndUserId(productId, userId)
          .stream()
          .map(reviewMapper::toDomain)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error loading reviews for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find reviews by product ID", e);
    }
  }

  private List<Review> getReviewsForProduct(UUID productId) {
    try {
      return jpaProductRepository.findReviewsByProductIdAndUserId(productId)
          .stream()
          .map(reviewMapper::toDomain)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error loading reviews for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find reviews by product ID", e);
    }
  }
}
