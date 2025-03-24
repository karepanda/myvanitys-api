package com.myvanitys.api.product.infrastructure.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.test.AbstractRepositoryIntegrationTest;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryIntegrationTest extends AbstractRepositoryIntegrationTest {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  void shouldSaveAndRetrieveProduct() {
    // Given
    CategoryEntity category = createSampleCategory("Test Category");
    CategoryEntity savedCategory = categoryRepository.save(category);

    ProductEntity product = createSampleProduct("Test Product", "Test Brand", savedCategory);

    // When
    ProductEntity savedProduct = productRepository.save(product);
    Optional<ProductEntity> retrievedProduct = productRepository.findById(savedProduct.getProductId());

    // Then
    assertThat(retrievedProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> {
          assertThat(productEntity.getName()).isEqualTo("Test Product");
          assertThat(productEntity.getBrand()).isEqualTo("Test Brand");
          assertThat(productEntity.getCategory().getName()).isEqualTo("Test Category");
        });
  }

  @Test
  void shouldFindProductByName() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = categoryRepository.save(category);

    ProductEntity product = createSampleProduct("Unique Product Name", "Brand X", savedCategory);
    productRepository.save(product);

    // When
    Optional<ProductEntity> foundProduct = productRepository.findByName("Unique Product Name");

    // Then
    assertThat(foundProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> assertThat(productEntity.getName()).isEqualTo("Unique Product Name"));
  }

  @Test
  void shouldNotFindProductByNonExistentName() {
    // When
    Optional<ProductEntity> foundProduct = productRepository.findByName("Non Existent Product");

    // Then
    assertThat(foundProduct).isEmpty();
  }

  @Test
  void shouldFindProductByBrand() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = categoryRepository.save(category);

    ProductEntity product = createSampleProduct("Some Product", "Unique Brand Name", savedCategory);
    productRepository.save(product);

    // When
    Optional<ProductEntity> foundProduct = productRepository.findByBrand("Unique Brand Name");

    // Then
    assertThat(foundProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> assertThat(productEntity.getBrand()).isEqualTo("Unique Brand Name"));
  }

  @Test
  void shouldNotFindProductByNonExistentBrand() {
    // When
    Optional<ProductEntity> foundProduct = productRepository.findByBrand("Non Existent Brand");

    // Then
    assertThat(foundProduct).isEmpty();
  }

  @Test
  void shouldFindProductsByCategoryId() {
    // Given
    CategoryEntity category1 = createSampleCategory("Electronics");
    CategoryEntity savedCategory1 = categoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Clothing");
    CategoryEntity savedCategory2 = categoryRepository.save(category2);

    ProductEntity product1 = createSampleProduct("Product 1", "Brand A", savedCategory1);
    ProductEntity product2 = createSampleProduct("Product 2", "Brand B", savedCategory1);
    ProductEntity product3 = createSampleProduct("Product 3", "Brand C", savedCategory2);

    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);

    // When
    List<ProductEntity> foundProducts = productRepository.findByCategoryCategoryId(savedCategory1.getCategoryId());

    // Then
    assertThat(foundProducts).hasSize(2);
    assertThat(foundProducts)
        .extracting(ProductEntity::getName)
        .containsExactlyInAnyOrder("Product 1", "Product 2");
  }

  @Test
  void shouldFindProductsByCategoryName() {
    // Given
    CategoryEntity category1 = createSampleCategory("Electronics");
    CategoryEntity savedCategory1 = categoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Clothing");
    CategoryEntity savedCategory2 = categoryRepository.save(category2);

    ProductEntity product1 = createSampleProduct("Product 1", "Brand A", savedCategory1);
    ProductEntity product2 = createSampleProduct("Product 2", "Brand B", savedCategory1);
    ProductEntity product3 = createSampleProduct("Product 3", "Brand C", savedCategory2);

    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);

    // When
    List<ProductEntity> foundProducts = productRepository.findByCategoryName("Electronics");

    // Then
    assertThat(foundProducts).hasSize(2);
    assertThat(foundProducts)
        .extracting(ProductEntity::getName)
        .containsExactlyInAnyOrder("Product 1", "Product 2");
  }

  @Test
  void shouldReturnEmptyListForNonExistentCategoryName() {
    // When
    List<ProductEntity> foundProducts = productRepository.findByCategoryName("Non Existent Category");

    // Then
    assertThat(foundProducts).isEmpty();
  }

  @Test
  void shouldUpdateExistingProduct() {
    // Given
    CategoryEntity category1 = createSampleCategory("Initial Category");
    CategoryEntity savedCategory1 = categoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Updated Category");
    CategoryEntity savedCategory2 = categoryRepository.save(category2);

    ProductEntity product = createSampleProduct("Initial Name", "Initial Brand", savedCategory1);
    ProductEntity savedProduct = productRepository.save(product);

    // When
    savedProduct.setName("Updated Name");
    savedProduct.setBrand("Updated Brand");
    savedProduct.setCategory(savedCategory2);
    productRepository.save(savedProduct);

    // Then
    Optional<ProductEntity> updatedProduct = productRepository.findById(savedProduct.getProductId());
    assertThat(updatedProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> {
          assertThat(productEntity.getName()).isEqualTo("Updated Name");
          assertThat(productEntity.getBrand()).isEqualTo("Updated Brand");
          assertThat(productEntity.getCategory().getName()).isEqualTo("Updated Category");
        });
  }

  @Test
  void shouldDeleteProduct() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = categoryRepository.save(category);

    ProductEntity product = createSampleProduct("Product to Delete", "Brand to Delete", savedCategory);
    ProductEntity savedProduct = productRepository.save(product);
    UUID id = savedProduct.getProductId();

    // When
    productRepository.deleteById(id);

    // Then
    Optional<ProductEntity> deletedProduct = productRepository.findById(id);
    assertThat(deletedProduct).isEmpty();
  }

  private CategoryEntity createSampleCategory(String name) {
    CategoryEntity category = new CategoryEntity();
    category.setName(name);
    return category;
  }

  private ProductEntity createSampleProduct(String name, String brand, CategoryEntity category) {
    ProductEntity product = new ProductEntity();
    product.setName(name);
    product.setBrand(brand);
    product.setColorHex("#FFFFFF");
    product.setCategory(category);
    return product;
  }
}