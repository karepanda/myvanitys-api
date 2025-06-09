package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.myvanitys.api.common.InfrastructureException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductUserRelationMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {

  private final CategoryRepository categoryRepository;

  private final ProductUserRepository productUserRepository;

  private final ReviewRepository reviewRepository;

  private final JpaProductRepository jpaProductRepository;

  private final ProductMapper productMapper;

  private final CategoryMapper categoryMapper;

  private final ProductUserRelationMapper productUserRelationMapper;

  private final ReviewMapper reviewMapper;

  @Override
  public Product save(Product product) {
    try {
      if (categoryRepository.findById(product.getCategory().categoryId()).isEmpty()) {
        throw new RepositoryResourceNotFoundException("Category not found with ID: " +
            product.getCategory().categoryId().getValue());
      }

      Optional<ProductEntity> existingProduct = jpaProductRepository.findById(product.getId().getValue());

      ProductEntity savedEntity;
      if (existingProduct.isPresent()) {
        savedEntity = existingProduct.get();
        savedEntity.setUpdatedAt(Instant.now());
        log.debug("Product already exists, updated timestamp: {}", product.getId().getValue());
      } else {
        ProductEntity entity = productMapper.toEntity(product);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        savedEntity = jpaProductRepository.save(entity);
        log.debug("Created new product: {}", product.getId().getValue());
      }

      validateProductUserRelationsExist(product);
      saveNewProductUserRelations(product);
      saveNewProductReviews(product);

      return reconstructProductWithAllData(savedEntity);

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
          .map(this::reconstructProductWithAllData);
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
            List<Review> reviews = getReviewsUserForProduct(productEntity.getProductId(), userId);
            Set<ProductUserRelation> relations = getRelationsForProduct(productEntity.getProductId());
            return Product.reconstruct(
                new EntityId(productEntity.getProductId()),
                productEntity.getName(),
                productEntity.getBrand(),
                category,
                productEntity.getColorHex(),
                reviews,
                relations
            );
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
            List<Review> reviews = getReviewsUserForProduct(productEntity.getProductId(), new EntityId(userId));
            Set<ProductUserRelation> relations = getRelationsForProduct(productEntity.getProductId());
            return Product.reconstruct(
                new EntityId(productEntity.getProductId()),
                productEntity.getName(),
                productEntity.getBrand(),
                category,
                productEntity.getColorHex(),
                reviews,
                relations
            );
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
          .map(this::reconstructProductWithAllData)
          .filter(Objects::nonNull)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error finding all products: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find all products", e);
    }
  }

  /**
   * Reconstructs a complete Product domain object with all its data
   */
  private Product reconstructProductWithAllData(ProductEntity productEntity) {
    Category category = getCategoryForProduct(productEntity);
    List<Review> reviews = getReviewsForProduct(productEntity.getProductId());
    Set<ProductUserRelation> relations = getRelationsForProduct(productEntity.getProductId());

    return Product.reconstruct(
        new EntityId(productEntity.getProductId()),
        productEntity.getName(),
        productEntity.getBrand(),
        category,
        productEntity.getColorHex(),
        reviews,
        relations
    );
  }

  private void validateProductUserRelationsExist(Product product) {
    product.getUserRelations().forEach(relation -> {
      if (!productUserRepository.existsByProductIdAndUserId(
          relation.getProductId(), relation.getUserId())) {
        throw new RepositoryResourceNotFoundException(
            "ProductUser relation must exist before adding reviews. Product: " +
                relation.getProductId().getValue() + ", User: " + relation.getUserId().getValue());
      }
    });
  }

  private void saveNewProductUserRelations(Product product) {
    product.getUserRelations().forEach(relation -> {
      try {
        productUserRepository.saveProductUserRelationship(
            relation.getProductId(), relation.getUserId());
      } catch (DataIntegrityViolationException e) {
        log.warn("Product relation {} already exists", relation.getProductId().getValue(), e);
      }
    });
  }

  private void saveNewProductReviews(Product product) {
    product.getReviews().stream()
        .filter(review -> reviewRepository.findById(review.getId()).isEmpty())
        .forEach(review -> {
          reviewRepository.save(review);
          log.debug("Saved new review: {}", review.getId().getValue());
        });
  }

  /**
   * Helper method to get a product's category using CategoryRepository
   */
  private Category getCategoryForProduct(ProductEntity productEntity) {
    try {
      UUID categoryId = productEntity.getCategoryId();
      if (categoryId == null) {
        return null;
      }

      return categoryRepository.findById(new EntityId(categoryId))
          .orElse(null);
    } catch (DataAccessException e) {
      log.error("Error loading category for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find category by ID", e);
    }
  }

  /**
   * ✅ OPTIMIZADO: Get reviews for a specific user and product using JpaProductRepository
   */
  private List<Review> getReviewsUserForProduct(UUID productId, EntityId userId) {
    try {
      List<ReviewEntity> reviewEntities = jpaProductRepository.findReviewsByProductIdAndUserId(
          productId, userId.getValue());

      return reviewEntities.stream()
          .map(reviewMapper::toDomain)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error loading reviews for product and user: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find reviews by product ID and user ID", e);
    }
  }

  /**
   * ✅ OPTIMIZADO: Get all reviews for a product using JpaProductRepository
   */
  private List<Review> getReviewsForProduct(UUID productId) {
    try {
      List<ReviewEntity> reviewEntities = jpaProductRepository.findReviewsByProductId(productId);

      return reviewEntities.stream()
          .map(reviewMapper::toDomain)
          .toList();
    } catch (DataAccessException e) {
      log.error("Error loading reviews for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find reviews by product ID", e);
    }
  }

  /**
   * Get all user relations for a product using ProductUserRepository
   */
  private Set<ProductUserRelation> getRelationsForProduct(UUID productId) {
    try {
      List<ProductUserRelation> relations = productUserRepository.findByProductId(productId);
      return Set.copyOf(relations);
    } catch (DataAccessException e) {
      log.error("Error loading relations for product: {}", e.getMessage(), e);
      throw DatabaseException.queryError("Find relations by product ID", e);
    }
  }
}