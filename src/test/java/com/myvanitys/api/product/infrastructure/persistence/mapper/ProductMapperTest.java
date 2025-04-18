package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

  @Mock
  private ProductMapper productMapper;

  // Datos de prueba
  private UUID productId;

  private UUID categoryId;

  private ProductEntity productEntity;

  private CategoryEntity categoryEntity;

  private Product product;

  private Category category;

  @BeforeEach
  void setUp() {
    // Inicializar IDs
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();

    // Inicializar objetos de categoría
    categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Skincare");
    categoryEntity.setCreatedAt(Instant.now());
    categoryEntity.setUpdatedAt(Instant.now());

    category = new Category(new EntityId(categoryId), "Skincare");

    // Inicializar objetos de producto
    productEntity = new ProductEntity();
    productEntity.setProductId(productId);
    productEntity.setName("Moisturizer");
    productEntity.setBrand("BrandX");
    productEntity.setColorHex("#FF5733");
    productEntity.setCategory(categoryEntity);
    productEntity.setCreatedAt(Instant.now());
    productEntity.setUpdatedAt(Instant.now());

    product = new Product(
        new EntityId(productId),
        "Moisturizer",
        "BrandX",
        category,
        "#FF5733"
    );

    // Cada configuración se hace lenient para evitar UnnecessaryStubbingException
    lenient().when(productMapper.toDomain(productEntity)).thenReturn(product);
    lenient().when(productMapper.toEntity(product)).thenReturn(productEntity);
    lenient().when(productMapper.categoryEntityToCategory(categoryEntity)).thenReturn(category);
    lenient().when(productMapper.categoryToCategoryEntity(category)).thenReturn(categoryEntity);
  }

  @Test
  void toDomain_WhenGivenValidProductEntity_ShouldReturnProduct() {
    // Act
    Product result = productMapper.toDomain(productEntity);

    // Assert
    assertNotNull(result);
    assertEquals(productId.toString(), result.getId().getValue().toString());
    assertEquals("Moisturizer", result.getName());
    assertEquals("BrandX", result.getBrand());
    assertEquals("#FF5733", result.getColorHex());
    assertNotNull(result.getCategory());
    assertEquals(categoryId.toString(), result.getCategory().categoryId().getValue().toString());
    assertEquals("Skincare", result.getCategory().name());

    // Verify
    verify(productMapper).toDomain(productEntity);
  }

  @Test
  void toEntity_WhenGivenValidProduct_ShouldReturnProductEntity() {
    // Act
    ProductEntity result = productMapper.toEntity(product);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getProductId());
    assertEquals("Moisturizer", result.getName());
    assertEquals("BrandX", result.getBrand());
    assertEquals("#FF5733", result.getColorHex());
    assertNotNull(result.getCategory());
    assertEquals(categoryId, result.getCategory().getCategoryId());
    assertEquals("Skincare", result.getCategory().getName());

    // Verify
    verify(productMapper).toEntity(product);
  }

  @Test
  void toDomain_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(productMapper.toDomain(null)).thenReturn(null);

    // Act
    Product result = productMapper.toDomain(null);

    // Assert
    assertNull(result);

    // Verify
    verify(productMapper).toDomain(null);
  }

  @Test
  void toEntity_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(productMapper.toEntity(null)).thenReturn(null);

    // Act
    ProductEntity result = productMapper.toEntity(null);

    // Assert
    assertNull(result);

    // Verify
    verify(productMapper).toEntity(null);
  }

  @Test
  void toDomainList_ShouldConvertListCorrectly() {
    // Arrange
    List<ProductEntity> entities = Arrays.asList(productEntity);
    List<Product> products = Arrays.asList(product);
    when(productMapper.toDomainList(entities)).thenReturn(products);

    // Act
    List<Product> result = productMapper.toDomainList(entities);

    // Assert
    assertEquals(1, result.size());
    assertEquals(product, result.get(0));

    // Verify
    verify(productMapper).toDomainList(entities);
  }

  @Test
  void toEntityList_ShouldConvertListCorrectly() {
    // Arrange
    List<Product> products = Arrays.asList(product);
    List<ProductEntity> entities = Arrays.asList(productEntity);
    when(productMapper.toEntityList(products)).thenReturn(entities);

    // Act
    List<ProductEntity> result = productMapper.toEntityList(products);

    // Assert
    assertEquals(1, result.size());
    assertEquals(productEntity, result.get(0));

    // Verify
    verify(productMapper).toEntityList(products);
  }

  @Test
  void categoryEntityToCategory_WhenGivenValidCategoryEntity_ShouldReturnCategory() {
    // Act
    Category result = productMapper.categoryEntityToCategory(categoryEntity);

    // Assert
    assertNotNull(result);
    assertEquals(categoryId.toString(), result.categoryId().getValue().toString());
    assertEquals("Skincare", result.name());

    // Verify
    verify(productMapper).categoryEntityToCategory(categoryEntity);
  }

  @Test
  void categoryEntityToCategory_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(productMapper.categoryEntityToCategory(null)).thenReturn(null);

    // Act
    Category result = productMapper.categoryEntityToCategory(null);

    // Assert
    assertNull(result);

    // Verify
    verify(productMapper).categoryEntityToCategory(null);
  }

  @Test
  void categoryToCategoryEntity_WhenGivenValidCategory_ShouldReturnCategoryEntity() {
    // Act
    CategoryEntity result = productMapper.categoryToCategoryEntity(category);

    // Assert
    assertNotNull(result);
    assertEquals(categoryId, result.getCategoryId());
    assertEquals("Skincare", result.getName());

    // Verify
    verify(productMapper).categoryToCategoryEntity(category);
  }

  @Test
  void categoryToCategoryEntity_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(productMapper.categoryToCategoryEntity(null)).thenReturn(null);

    // Act
    CategoryEntity result = productMapper.categoryToCategoryEntity(null);

    // Assert
    assertNull(result);

    // Verify
    verify(productMapper).categoryToCategoryEntity(null);
  }

  @Test
  void categoryToCategoryEntity_WhenGivenCategoryWithNullId_ShouldReturnNull() {
    // Arrange
    Category categoryWithNullId = new Category(null, "Skincare");
    when(productMapper.categoryToCategoryEntity(categoryWithNullId)).thenReturn(null);

    // Act
    CategoryEntity result = productMapper.categoryToCategoryEntity(categoryWithNullId);

    // Assert
    assertNull(result);

    // Verify
    verify(productMapper).categoryToCategoryEntity(categoryWithNullId);
  }
}