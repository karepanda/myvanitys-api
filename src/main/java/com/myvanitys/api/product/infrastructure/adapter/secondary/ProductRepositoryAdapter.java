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
      // Verificar que la categoría existe antes de guardar el producto
      UUID categoryId = product.getCategory().categoryId().getValue();
      if (!jpaCategoryRepository.existsById(categoryId)) {
        throw new RepositoryResourceNotFoundException("Category not found with ID: " + categoryId);
      }

      // Convertir producto a entidad
      ProductEntity entity = productMapper.toEntity(product);

      // Establecer campos de auditoría si es una nueva entidad
      if (entity.getCreatedAt() == null) {
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
      } else {
        entity.setUpdatedAt(Instant.now());
      }

      // Guardar la entidad
      ProductEntity savedEntity = jpaProductRepository.save(entity);

      // Devolver el producto con su categoría
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
            // Cargar la categoría asociada
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
            // Cargar la categoría
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

            // Convertir la categoría a objeto de dominio
            Category category = categoryMapper.toDomain(categoryEntity);

            // Mapear cada producto con la misma categoría
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
      // Obtener las IDs de productos asociados al usuario
      List<EntityId> productIds = productUserRepository.findProductIdsByUserId(new EntityId(userId));

      // Si no hay productos, devolver lista vacía
      if (productIds.isEmpty()) {
        return Collections.emptyList();
      }

      // Convertir EntityId a UUID para la consulta
      List<UUID> productUuids = productIds.stream()
          .map(EntityId::getValue)
          .toList();

      // Obtener las entidades de productos
      List<ProductEntity> productEntities = jpaProductRepository.findAllById(productUuids);

      // Convertir a objetos de dominio cargando sus categorías
      return productEntities.stream()
          .map(productEntity -> {
            // Cargar la categoría para cada producto
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
   * Método auxiliar para obtener la categoría de un producto
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