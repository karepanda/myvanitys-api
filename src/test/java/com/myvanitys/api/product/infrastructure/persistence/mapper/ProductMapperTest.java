package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

  @Mock
  private CategoryMapper categoryMapper;

  @Mock
  private EntityIdMapper entityIdMapper;

  @Mock
  private JpaCategoryRepository jpaCategoryRepository;

  @InjectMocks
  private ProductMapperImpl productMapper;

  private UUID productId;

  private UUID categoryId;

  private UUID userId;

  private UUID reviewId;

  private UUID productUserId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();
    userId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    productUserId = UUID.randomUUID();
  }

  @Test
  void toDomain_shouldMapProductEntityToProduct() {
    // Arrange
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    ProductEntity productEntity = new ProductEntity();
    productEntity.setProductId(productId);
    productEntity.setName("Test Product");
    productEntity.setBrand("Test Brand");
    productEntity.setColorHex("#FFFFFF");
    productEntity.setCategory(categoryEntity);

    EntityId entityId = new EntityId(productId);
    Category category = new Category(new EntityId(categoryId), "Test Category");

    when(entityIdMapper.toEntityId(productId)).thenReturn(entityId);
    when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);

    // Act
    Product result = productMapper.toDomain(productEntity);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId().getValue());
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals("#FFFFFF", result.getColorHex());
    assertEquals(category, result.getCategory());
    assertTrue(result.getReviews().isEmpty());
    assertTrue(result.getUserRelations().isEmpty());
  }

  @Test
  void toEntity_shouldMapProductToProductEntity() {
    // Arrange
    Category category = new Category(new EntityId(categoryId), "Test Category");
    Product product = new Product(
        new EntityId(productId),
        "Test Product",
        "Test Brand",
        category,
        "#FFFFFF"
    );

    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    // Mock the category repository for the @AfterMapping method
    when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));

    // Act
    ProductEntity result = productMapper.toEntity(product);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getProductId());
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals("#FFFFFF", result.getColorHex());
    assertEquals(categoryEntity, result.getCategory());

    // Verify that the repository was called
    verify(jpaCategoryRepository).findById(categoryId);
  }

  @Test
  void toEntity_shouldThrowExceptionWhenCategoryNotFound() {
    // Arrange
    Category category = new Category(new EntityId(categoryId), "Test Category");
    Product product = new Product(
        new EntityId(productId),
        "Test Product",
        "Test Brand",
        category,
        "#FFFFFF"
    );

    // Mock the category repository to return empty
    when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(DatabaseException.class, () -> productMapper.toEntity(product));
  }

  @Test
  void toDomainWithRelations_shouldMapProductWithReviewsAndRelations() {
    // Arrange
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    ProductEntity productEntity = new ProductEntity();
    productEntity.setProductId(productId);
    productEntity.setName("Test Product");
    productEntity.setBrand("Test Brand");
    productEntity.setColorHex("#FFFFFF");
    productEntity.setCategory(categoryEntity);

    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserId);
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);

    ReviewEntity reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(reviewId);
    reviewEntity.setRating(5);
    reviewEntity.setComment("Great product!");
    reviewEntity.setProductUserEntity(productUserEntity);

    productUserEntity.setReviews(List.of(reviewEntity));

    EntityId entityId = new EntityId(productId);
    Category category = new Category(new EntityId(categoryId), "Test Category");

    when(entityIdMapper.toEntityId(productId)).thenReturn(entityId);
    when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);

    // Act
    Product result = productMapper.toDomainWithRelations(productEntity, List.of(productUserEntity));

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId().getValue());
    assertEquals("Test Product", result.getName());
    assertEquals(1, result.getReviews().size());
    assertEquals(1, result.getUserRelations().size());

    Review review = result.getReviews().getFirst();
    assertEquals(reviewId, review.getId().getValue());
    assertEquals(userId, review.getUserId().getValue());
    assertEquals(5, review.getRating());
    assertEquals("Great product!", review.getComment());

    ProductUserRelation relation = result.getUserRelations().iterator().next();
    assertEquals(productUserId, relation.getId().getValue());
    assertEquals(productId, relation.getProductId().getValue());
    assertEquals(userId, relation.getUserId().getValue());
    assertEquals(reviewId, relation.getReviewId().getValue());
  }

  @Test
  void toReviewEntity_shouldMapReviewToReviewEntity() {
    // Arrange
    Category category = new Category(new EntityId(categoryId), "Test Category");
    Product product = new Product(
        new EntityId(productId),
        "Test Product",
        "Test Brand",
        category,
        "#FFFFFF"
    );

    Review review = new Review(
        new EntityId(reviewId),
        new EntityId(userId),
        product,
        5,
        "Great product!"
    );

    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(productUserId);
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);

    // Act
    ReviewEntity result = productMapper.toReviewEntity(review, productUserEntity);

    // Assert
    assertNotNull(result);
    assertEquals(reviewId, result.getReviewId());
    assertEquals(5, result.getRating());
    assertEquals("Great product!", result.getComment());
    assertEquals(productUserEntity, result.getProductUserEntity());
  }

  @Test
  void toDomainList_shouldMapProductEntityListToProductList() {
    // Arrange
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    ProductEntity productEntity1 = createProductEntity(UUID.randomUUID(), "Product 1", categoryEntity);
    ProductEntity productEntity2 = createProductEntity(UUID.randomUUID(), "Product 2", categoryEntity);

    EntityId entityId1 = new EntityId(productEntity1.getProductId());
    EntityId entityId2 = new EntityId(productEntity2.getProductId());
    Category category = new Category(new EntityId(categoryId), "Test Category");

    when(entityIdMapper.toEntityId(productEntity1.getProductId())).thenReturn(entityId1);
    when(entityIdMapper.toEntityId(productEntity2.getProductId())).thenReturn(entityId2);
    when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);

    // Act
    List<Product> result = productMapper.toDomainList(List.of(productEntity1, productEntity2));

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Product 1", result.get(0).getName());
    assertEquals("Product 2", result.get(1).getName());
  }

  @Test
  void toEntityList_shouldMapProductListToProductEntityList() {
    // Arrange
    Category category = new Category(new EntityId(categoryId), "Test Category");
    Product product1 = createProduct(UUID.randomUUID(), "Product 1", category);
    Product product2 = createProduct(UUID.randomUUID(), "Product 2", category);

    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    // Mock the category repository for the @AfterMapping method
    when(jpaCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));

    // Act
    List<ProductEntity> result = productMapper.toEntityList(List.of(product1, product2));

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Product 1", result.get(0).getName());
    assertEquals("Product 2", result.get(1).getName());

    // Verify that the repository was called for each product
    verify(jpaCategoryRepository, times(2)).findById(categoryId);
  }

  private ProductEntity createProductEntity(UUID id, String name, CategoryEntity category) {
    ProductEntity entity = new ProductEntity();
    entity.setProductId(id);
    entity.setName(name);
    entity.setBrand("Test Brand");
    entity.setColorHex("#FFFFFF");
    entity.setCategory(category);
    return entity;
  }

  private Product createProduct(UUID id, String name, Category category) {
    return new Product(
        new EntityId(id),
        name,
        "Test Brand",
        category,
        "#FFFFFF"
    );
  }
}