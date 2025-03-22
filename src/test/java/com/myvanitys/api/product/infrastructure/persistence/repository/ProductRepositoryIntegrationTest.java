package com.myvanitys.api.product.infrastructure.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.test.AbstractRepositoryIntegrationTest;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryIntegrationTest extends AbstractRepositoryIntegrationTest {

  @Autowired
  private ProductRepository productRepository;

  @Test
  void shouldSaveAndRetrieveProduct() {
    // Given
    ProductEntity product = createSampleProduct("Test Product", "Test Brand");

    // When
    ProductEntity savedProduct = productRepository.save(product);
    Optional<ProductEntity> retrievedProduct = productRepository.findById(savedProduct.getProductId());

    // Then
    assertThat(retrievedProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> {
          assertThat(productEntity.getName()).isEqualTo("Test Product");
          assertThat(productEntity.getBrand()).isEqualTo("Test Brand");
        });
  }

  @Test
  void shouldFindProductByName() {
    // Given
    ProductEntity product = createSampleProduct("Unique Product Name", "Brand X");
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
    ProductEntity product = createSampleProduct("Some Product", "Unique Brand Name");
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
  void shouldUpdateExistingProduct() {
    // Given
    ProductEntity product = createSampleProduct("Initial Name", "Initial Brand");
    ProductEntity savedProduct = productRepository.save(product);

    // When
    savedProduct.setName("Updated Name");
    savedProduct.setBrand("Updated Brand");
    productRepository.save(savedProduct);

    // Then
    Optional<ProductEntity> updatedProduct = productRepository.findById(savedProduct.getProductId());
    assertThat(updatedProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> {
          assertThat(productEntity.getName()).isEqualTo("Updated Name");
          assertThat(productEntity.getBrand()).isEqualTo("Updated Brand");
        });
  }

  @Test
  void shouldDeleteProduct() {
    // Given
    ProductEntity product = createSampleProduct("Product to Delete", "Brand to Delete");
    ProductEntity savedProduct = productRepository.save(product);
    UUID id = savedProduct.getProductId();

    // When
    productRepository.deleteById(id);

    // Then
    Optional<ProductEntity> deletedProduct = productRepository.findById(id);
    assertThat(deletedProduct).isEmpty();
  }

  private ProductEntity createSampleProduct(String name, String brand) {
    ProductEntity product = new ProductEntity();
    product.setName(name);
    product.setBrand(brand);
    product.setColorHex("#FFFFFF");
    // No necesitas establecer createdAt ni updatedAt, ya que se manejan con @PrePersist y @PreUpdate
    return product;
  }
}
