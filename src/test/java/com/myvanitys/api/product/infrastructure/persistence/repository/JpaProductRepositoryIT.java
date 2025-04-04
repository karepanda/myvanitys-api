package com.myvanitys.api.product.infrastructure.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.Test;

class JpaProductRepositoryIT extends AbstractJpaTest {

  @Test
  void shouldSaveAndRetrieveProductWithReviews() {
    // Given
    CategoryEntity category = createSampleCategory("Test Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct("Test Product", "Test Brand", savedCategory);

    // When
    ProductEntity savedProduct = jpaProductRepository.save(product);

    ReviewEntity review = new ReviewEntity();
    review.setRating(5);
    review.setComment("Great product!");
    ProductUserEntity productUser = new ProductUserEntity();
    productUser.setProductId(savedProduct.getProductId());
    final var userid = UUID.randomUUID();
    productUser.setUserId(userid);
    review.setProductUserEntity(productUser);
    productUser.setReviews(List.of(review));
    ProductUserEntity savedProductUser = jpaProductUserRepository.save(productUser);

    Optional<ProductEntity> retrievedProduct = jpaProductRepository.findById(savedProduct.getProductId());

    ProductUserEntity retrievedProductUser =
        jpaProductUserRepository.findByProductIdAndUserId(savedProductUser.getProductId(), savedProductUser.getUserId());

    // Then
    assertThat(retrievedProduct)
        .isPresent()
        .get()
        .satisfies(productEntity -> {
          assertThat(productEntity)
              .extracting(ProductEntity::getName, ProductEntity::getBrand)
              .containsExactly("Test Product", "Test Brand");

          assertThat(productEntity.getCategory())
              .extracting(CategoryEntity::getName)
              .isEqualTo("Test Category");
        });

    assertThat(retrievedProductUser)
        .extracting(ProductUserEntity::getUserId, ProductUserEntity::getProductId)
        .containsExactly(userid, product.getProductId());

    assertThat(retrievedProductUser.getReviews())
        .hasSize(1)
        .first()
        .satisfies(r -> assertThat(r)
            .extracting(ReviewEntity::getRating, ReviewEntity::getComment)
            .containsExactly(5, "Great product!")
        );

  }

  @Test
  void shouldFindProductByName() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct("Unique Product Name", "Brand X", savedCategory);
    jpaProductRepository.save(product);

    // When
    Optional<ProductEntity> foundProduct = jpaProductRepository.findByName("Unique Product Name");

    // Then
    assertThat(foundProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> assertThat(productEntity.getName()).isEqualTo("Unique Product Name"));
  }

  @Test
  void shouldNotFindProductByNonExistentName() {
    // When
    Optional<ProductEntity> foundProduct = jpaProductRepository.findByName("Non Existent Product");

    // Then
    assertThat(foundProduct).isEmpty();
  }

  @Test
  void shouldFindProductByBrand() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct("Some Product", "Unique Brand Name", savedCategory);
    jpaProductRepository.save(product);

    // When
    Optional<ProductEntity> foundProduct = jpaProductRepository.findByBrand("Unique Brand Name");

    // Then
    assertThat(foundProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> assertThat(productEntity.getBrand()).isEqualTo("Unique Brand Name"));
  }

  @Test
  void shouldNotFindProductByNonExistentBrand() {
    // When
    Optional<ProductEntity> foundProduct = jpaProductRepository.findByBrand("Non Existent Brand");

    // Then
    assertThat(foundProduct).isEmpty();
  }

  @Test
  void shouldFindProductsByCategoryId() {
    // Given
    CategoryEntity category1 = createSampleCategory("Electronics");
    CategoryEntity savedCategory1 = jpaCategoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Clothing");
    CategoryEntity savedCategory2 = jpaCategoryRepository.save(category2);

    ProductEntity product1 = createSampleProduct("Product 1", "Brand A", savedCategory1);
    ProductEntity product2 = createSampleProduct("Product 2", "Brand B", savedCategory1);
    ProductEntity product3 = createSampleProduct("Product 3", "Brand C", savedCategory2);

    jpaProductRepository.save(product1);
    jpaProductRepository.save(product2);
    jpaProductRepository.save(product3);

    // When
    List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryCategoryId(savedCategory1.getCategoryId());

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
    CategoryEntity savedCategory1 = jpaCategoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Clothing");
    CategoryEntity savedCategory2 = jpaCategoryRepository.save(category2);

    ProductEntity product1 = createSampleProduct("Product 1", "Brand A", savedCategory1);
    ProductEntity product2 = createSampleProduct("Product 2", "Brand B", savedCategory1);
    ProductEntity product3 = createSampleProduct("Product 3", "Brand C", savedCategory2);

    jpaProductRepository.save(product1);
    jpaProductRepository.save(product2);
    jpaProductRepository.save(product3);

    // When
    List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryName("Electronics");

    // Then
    assertThat(foundProducts).hasSize(2);
    assertThat(foundProducts)
        .extracting(ProductEntity::getName)
        .containsExactlyInAnyOrder("Product 1", "Product 2");
  }

  @Test
  void shouldReturnEmptyListForNonExistentCategoryName() {
    // When
    List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryName("Non Existent Category");

    // Then
    assertThat(foundProducts).isEmpty();
  }

  @Test
  void shouldUpdateExistingProduct() {
    // Given
    CategoryEntity category1 = createSampleCategory("Initial Category");
    CategoryEntity savedCategory1 = jpaCategoryRepository.save(category1);

    CategoryEntity category2 = createSampleCategory("Updated Category");
    CategoryEntity savedCategory2 = jpaCategoryRepository.save(category2);

    ProductEntity product = createSampleProduct("Initial Name", "Initial Brand", savedCategory1);
    ProductEntity savedProduct = jpaProductRepository.save(product);

    // When
    savedProduct.setName("Updated Name");
    savedProduct.setBrand("Updated Brand");
    savedProduct.setCategory(savedCategory2);
    jpaProductRepository.save(savedProduct);

    // Then
    Optional<ProductEntity> updatedProduct = jpaProductRepository.findById(savedProduct.getProductId());
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
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct("Product to Delete", "Brand to Delete", savedCategory);
    ProductEntity savedProduct = jpaProductRepository.save(product);
    UUID id = savedProduct.getProductId();

    // When
    jpaProductRepository.deleteById(id);

    // Then
    Optional<ProductEntity> deletedProduct = jpaProductRepository.findById(id);
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