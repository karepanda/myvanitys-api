package com.myvanitys.api.product.infrastructure.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;

class JpaProductRepositoryIT extends AbstractJpaProductTest {

  @Test
  void shouldSaveAndRetrieveProductWithReviews() {
    // Given
    CategoryEntity category = createSampleCategory("Test Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);
    jpaCategoryRepository.flush();

    ProductEntity product = createSampleProduct("Test Product", "Test Brand", savedCategory);

    // When
    ProductEntity savedProduct = jpaProductRepository.save(product);
    jpaProductRepository.flush();

    // Crear y guardar el ProductUserEntity
    ProductUserEntity productUser = new ProductUserEntity();
    productUser.setProductUserId(UUID.randomUUID());
    productUser.setProductId(savedProduct.getProductId());
    final var userid = UUID.randomUUID();
    productUser.setProductUserId(UUID.randomUUID());
    productUser.setUserId(userid);

    ProductUserEntity savedProductUser = jpaProductUserRepository.save(productUser);
    jpaProductUserRepository.flush();

    // Luego crear y guardar la ReviewEntity
    ReviewEntity review = new ReviewEntity();
    review.setReviewId(UUID.randomUUID());
    review.setRating(5);
    review.setComment("Great product!");
    review.setProductUserId(savedProductUser.getProductUserId());

    jpaReviewRepository.save(review);
    jpaReviewRepository.flush();

    Optional<ProductEntity> retrievedProduct = jpaProductRepository.findById(savedProduct.getProductId());

    Optional<ProductUserEntity> retrievedProductUser =
        jpaProductUserRepository.findByProductIdAndUserId(savedProductUser.getProductId(), savedProductUser.getUserId());

    // Then
    assertThat(retrievedProduct)
        .isPresent()
        .get()
        .satisfies(productEntity -> {
          assertThat(productEntity)
              .extracting(ProductEntity::getName, ProductEntity::getBrand)
              .containsExactly("Test Product", "Test Brand");

          assertThat(productEntity.getCategoryId()).isEqualTo(category.getCategoryId());
        });

    assertThat(retrievedProductUser)
        .isPresent()
        .get()
        .satisfies(productUserEntity -> assertThat(productUserEntity)
            .extracting(ProductUserEntity::getUserId, ProductUserEntity::getProductId)
            .containsExactly(userid, product.getProductId()));

    // Verificar que al recuperar las reviews asociadas se obtiene la review guardada
    List<ReviewEntity> reviews = jpaReviewRepository.findByProductUserId(savedProductUser.getProductUserId());
    assertThat(reviews)
        .hasSize(1)
        .first()
        .satisfies(r -> assertThat(r)
            .extracting(ReviewEntity::getRating, ReviewEntity::getComment)
            .containsExactly(5, "Great product!")
        );
  }

  @Test
  void shouldFindAllProducts() {
    // Given
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    CategoryEntity category1 = createSampleCategory("Some Category");
    CategoryEntity savedCategory1 = jpaCategoryRepository.save(category1);

    ProductEntity product = createSampleProduct("Unique Product Name", "Brand X", savedCategory);
    ProductEntity product1 = createSampleProduct("Unique Product Name1", "Brand X1", savedCategory1);

    jpaProductRepository.save(product);
    jpaProductRepository.save(product1);

    // When
    List<ProductEntity> foundProducts = jpaProductRepository.findAll();

    // Then
    assertThat(foundProducts).isNotEmpty();
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
    List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryId(savedCategory1.getCategoryId());

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

    // When: Find products by category name using a join query
    List<ProductEntity> foundProducts = jpaProductRepository.findByCategoryName("Electronics");

    // Then: Assert that the correct products are found
    assertThat(foundProducts).hasSize(2);
    assertThat(foundProducts)
        .extracting(ProductEntity::getName)
        .containsExactlyInAnyOrder("Product 1", "Product 2");

    // Additional verification for the second category
    List<ProductEntity> clothingProducts = jpaProductRepository.findByCategoryName("Clothing");
    assertThat(clothingProducts).hasSize(1);
    assertThat(clothingProducts)
        .extracting(ProductEntity::getName)
        .containsExactly("Product 3");
  }

  @Test
  void shouldReturnEmptyListForNonExistentCategoryName() {
    // When
    List<ProductEntity> foundProducts = jpaProductRepository.findByName("Non Existent Category").stream().toList();

    // Then
    assertThat(foundProducts).isEmpty();
  }

  @Test
  void shouldUpdateExistingProduct() {
    // Given
    CategoryEntity category1 = createSampleCategory("Initial Category");
    CategoryEntity savedCategory1 = jpaCategoryRepository.save(category1);
    jpaCategoryRepository.flush();

    CategoryEntity category2 = createSampleCategory("Updated Category");
    CategoryEntity savedCategory2 = jpaCategoryRepository.save(category2);
    jpaCategoryRepository.flush();

    ProductEntity product = createSampleProduct("Initial Name", "Initial Brand", savedCategory1);
    ProductEntity savedProduct = jpaProductRepository.save(product);
    jpaProductRepository.flush();

    // Verifica que la versión no sea null antes de modificar
    Long initialVersion = savedProduct.getVersion();

    // When
    savedProduct.setName("Updated Name");
    savedProduct.setBrand("Updated Brand");
    savedProduct.setCategoryId(savedCategory2.getCategoryId());
    jpaProductRepository.save(savedProduct);
    jpaProductRepository.flush();

    // Then
    Optional<ProductEntity> updatedProduct = jpaProductRepository.findById(savedProduct.getProductId());
    assertThat(updatedProduct)
        .isPresent()
        .hasValueSatisfying(productEntity -> {
          assertThat(productEntity.getName()).isEqualTo("Updated Name");
          assertThat(productEntity.getBrand()).isEqualTo("Updated Brand");
          assertThat(productEntity.getCategoryId()).isEqualTo(category2.getCategoryId());
          assertThat(productEntity.getVersion()).isNotEqualTo(initialVersion);
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

  @Test
  void shouldFindByNameOrBrandWhenExistNameReturn() {
    // Given
    final String searchTerm = "Unique Product Name";
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct(searchTerm, Strings.EMPTY, savedCategory);
    jpaProductRepository.save(product);

    // When
    List<ProductEntity> foundProduct = jpaProductRepository.searchByNameOrBrand("product name");

    // Then
    assertTrue(foundProduct.stream().allMatch(productEntity -> productEntity.getName().equals(searchTerm)));

  }

  @Test
  void shouldFindByNameOrBrandWhenExistBrandReturn() {
    // Given
    final String searchTerm = "Unique Product Brand";
    CategoryEntity category = createSampleCategory("Some Category");
    CategoryEntity savedCategory = jpaCategoryRepository.save(category);

    ProductEntity product = createSampleProduct(Strings.EMPTY, searchTerm, savedCategory);
    jpaProductRepository.save(product);

    // When
    List<ProductEntity> foundProduct = jpaProductRepository.searchByNameOrBrand("product brand");

    // Then
    assertTrue(foundProduct.stream().allMatch(productEntity -> productEntity.getBrand().equals(searchTerm)));
  }

  private CategoryEntity createSampleCategory(String name) {
    CategoryEntity category = new CategoryEntity();
    category.setName(name);
    return category;
  }

  private ProductEntity createSampleProduct(String name, String brand, CategoryEntity category) {
    ProductEntity product = new ProductEntity();
    product.setProductId(UUID.randomUUID());
    product.setName(name);
    product.setBrand(brand);
    product.setColorHex("#FFFFFF");
    product.setCategoryId(category.getCategoryId());
    return product;
  }
}
