package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.exception.RepositoryResourceNotFoundException;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private JpaCategoryRepository jpaCategoryRepository;

  @Mock
  private ProductUserRepository productUserRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private ProductRepositoryAdapter target;

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    void when_givenValidProduct_then_returnsSavedProduct() {
      // Given
      final UUID categoryId = UUID.randomUUID();
      final EntityId categoryEntityId = new EntityId(categoryId);
      final Category category = new Category(categoryEntityId, "Test Category");

      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          null,
          null
      );

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);
      productEntity.setName("Test Product");

      final ProductEntity savedProductEntity = new ProductEntity();
      savedProductEntity.setProductId(productId);
      savedProductEntity.setCategoryId(categoryId);
      savedProductEntity.setName("Test Product");
      savedProductEntity.setCreatedAt(Instant.now());

      when(jpaCategoryRepository.existsById(categoryId)).thenReturn(true);
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(productEntity)).thenReturn(savedProductEntity);
      when(productMapper.toDomain(savedProductEntity, category, any())).thenReturn(product);

      // When
      final Product result = target.save(product);

      // Then
      assertThat(result).isEqualTo(product);
      verify(jpaCategoryRepository).existsById(categoryId);
      verify(productMapper).toEntity(product);
      verify(jpaProductRepository).save(productEntity);
      verify(productMapper).toDomain(savedProductEntity, category, List.of());
    }

    @Test
    void when_categoryDoesNotExist_then_throwsRepositoryResourceNotFoundException() {
      // Given
      final UUID categoryId = UUID.randomUUID();
      final EntityId categoryEntityId = new EntityId(categoryId);
      final Category category = new Category(categoryEntityId, "Test Category");

      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          null,
          null
      );

      when(jpaCategoryRepository.existsById(categoryId)).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> target.save(product))
          .isInstanceOf(RepositoryResourceNotFoundException.class)
          .hasMessageContaining("Category not found");

      verify(productMapper, never()).toEntity(any());
      verify(jpaProductRepository, never()).save(any());
    }

    @Test
    void when_dataAccessExceptionOccurs_then_throwsDatabaseException() {
      // Given
      final UUID categoryId = UUID.randomUUID();
      final EntityId categoryEntityId = new EntityId(categoryId);
      final Category category = new Category(categoryEntityId, "Test Category");

      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);
      final Product product = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          null,
          null
      );

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);

      when(jpaCategoryRepository.existsById(categoryId)).thenReturn(true);
      when(productMapper.toEntity(product)).thenReturn(productEntity);
      when(jpaProductRepository.save(productEntity)).thenThrow(mock(DataAccessException.class));

      // When & Then
      assertThatThrownBy(() -> target.save(product))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Save product");
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    void when_productExists_then_returnsProduct() {
      // Given
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);

      final UUID categoryId = UUID.randomUUID();

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);
      productEntity.setName("Test Product");

      final CategoryEntity categoryEntity = new CategoryEntity();
      categoryEntity.setCategoryId(categoryId);
      categoryEntity.setName("Test Category");

      final Category category = new Category(new EntityId(categoryId), "Test Category");

      final Product expectedProduct = Product.reconstruct(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF",
          null,
          null
      );

      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
      when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);
      when(productMapper.toDomain(productEntity, category, any())).thenReturn(expectedProduct);

      // When
      final Optional<Product> result = target.findById(productEntityId);

      // Then
      assertThat(result).isPresent();
      assertThat(result).contains(expectedProduct);
    }

    // Otros tests de FindById sin cambios en la lógica...
  }

  @Nested
  @DisplayName("findByName")
  class FindByName {

    @Test
    void when_productExists_then_returnsProduct() {
      // Given
      final String productName = "Test Product";

      final UUID productId = UUID.randomUUID();
      final UUID categoryId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);
      productEntity.setName(productName);

      final CategoryEntity categoryEntity = new CategoryEntity();
      categoryEntity.setCategoryId(categoryId);
      categoryEntity.setName("Test Category");

      final Category category = new Category(new EntityId(categoryId), "Test Category");

      final Product expectedProduct = Product.reconstruct(
          new EntityId(productId),
          productName,
          "Test Brand",
          category,
          "#FFFFFF",
          null,
          null
      );

      when(jpaProductRepository.findByName(productName)).thenReturn(Optional.of(productEntity));
      when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
      when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);
      when(productMapper.toDomain(productEntity, category, any())).thenReturn(expectedProduct);

      // When
      final Optional<Product> result = target.findByName(productName, EntityId.of(userId));

      // Then
      assertThat(result).isPresent();
      assertThat(result).contains(expectedProduct);
    }

    // Otros tests de FindByName sin cambios en la lógica...
  }

  @Nested
  @DisplayName("findByUserId")
  class FindByUserId {

    @Test
    void when_userHasProducts_then_returnsListOfProducts() {
      // Given
      final UUID userId = UUID.randomUUID();
      final EntityId userEntityId = new EntityId(userId);

      // Definir IDs de productos
      final UUID productId1 = UUID.randomUUID();
      final UUID productId2 = UUID.randomUUID();
      final EntityId productEntityId1 = new EntityId(productId1);
      final EntityId productEntityId2 = new EntityId(productId2);

      // Definir IDs de categorías
      final UUID categoryId1 = UUID.randomUUID();
      final UUID categoryId2 = UUID.randomUUID();
      final EntityId categoryEntityId1 = new EntityId(categoryId1);
      final EntityId categoryEntityId2 = new EntityId(categoryId2);

      // Lista de IDs de productos
      final List<EntityId> productIds = Arrays.asList(productEntityId1, productEntityId2);

      // Crear entidades de producto con IDs de categoría específicos
      final ProductEntity productEntity1 = new ProductEntity();
      productEntity1.setProductId(productId1);
      productEntity1.setName("Product 1");
      productEntity1.setCategoryId(categoryId1); // Usar el ID de categoría 1

      final ProductEntity productEntity2 = new ProductEntity();
      productEntity2.setProductId(productId2);
      productEntity2.setName("Product 2");
      productEntity2.setCategoryId(categoryId2); // Usar el ID de categoría 2

      final List<ProductEntity> productEntities = Arrays.asList(productEntity1, productEntity2);

      // Crear categorías con los mismos IDs que se usaron en las entidades
      final Category category1 = new Category(categoryEntityId1, "Category 1");
      final Category category2 = new Category(categoryEntityId2, "Category 2");

      // Crear productos de dominio
      final Product product1 = Product.reconstruct(
          productEntityId1,
          "Product 1",
          "Brand 1",
          category1,
          "#FFFFFF",
          null,
          null
      );

      final Product product2 = Product.reconstruct(
          productEntityId2,
          "Product 2",
          "Brand 2",
          category2,
          "#000000",
          null,
          null
      );

      // Configurar mocks para repositorios
      when(productUserRepository.findProductIdsByUserId(eq(userEntityId))).thenReturn(productIds);
      when(jpaProductRepository.findAllById(Arrays.asList(productId1, productId2))).thenReturn(productEntities);

      // Configurar entidades de categoría
      final CategoryEntity categoryEntity1 = new CategoryEntity();
      categoryEntity1.setCategoryId(categoryId1);
      final CategoryEntity categoryEntity2 = new CategoryEntity();
      categoryEntity2.setCategoryId(categoryId2);

      // Configurar mocks para categorías
      when(jpaCategoryRepository.findById(categoryId1)).thenReturn(Optional.of(categoryEntity1));
      when(jpaCategoryRepository.findById(categoryId2)).thenReturn(Optional.of(categoryEntity2));
      when(categoryMapper.toDomain(categoryEntity1)).thenReturn(category1);
      when(categoryMapper.toDomain(categoryEntity2)).thenReturn(category2);

      // Configurar mocks para mapper de productos
      when(productMapper.toDomain(productEntity1, category1, any())).thenReturn(product1);
      when(productMapper.toDomain(productEntity2, category2, any())).thenReturn(product2);

      // When
      final List<Product> result = target.findByUserId(userId);

      // Then
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(product1, product2);
    }

    // Otros tests de FindByUserId sin cambios en la lógica...
  }

  // Clase DeleteById sin cambios...
}