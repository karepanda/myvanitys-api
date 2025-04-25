package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

  @InjectMocks
  private ProductMapperImpl productMapper;

  private UUID productId;
  private UUID productUserId;
  private UUID categoryId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();
    productUserId = UUID.randomUUID();
  }

@Test
void toDomain_shouldReturnNullWhenProductEntityIsNull() {
    // Act
    Product result = productMapper.toDomain(null);

    // Assert
    assertNull(result);
}

@Test
void toDomain_withoutCategory_shouldMapProductEntityToProduct() {
    // Arrange
    ProductEntity productEntity = new ProductEntity();
    productEntity.setProductId(productId);
    productEntity.setName("Test Product");
    productEntity.setBrand("Test Brand");
    productEntity.setColorHex("#FFFFFF");

    // Act
    Product result = productMapper.toDomain(productEntity);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId().getValue());
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals("#FFFFFF", result.getColorHex());
    assertNull(result.getCategory());
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
    productEntity.setCategoryId(categoryId);

    Category category = new Category(new EntityId(categoryId), "Test Category");

    // Act
    Product result = productMapper.toDomain(productEntity, category);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId().getValue());
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals("#FFFFFF", result.getColorHex());
    assertEquals(category, result.getCategory());
  }

@Test
void toEntity_shouldReturnNullWhenProductIsNull() {
    // Act
    ProductEntity result = productMapper.toEntity(null);

    // Assert
    assertNull(result);
}

  @Test
  void toEntity_shouldMapProductToProductEntity() {
    // Arrange
    Category category = new Category(new EntityId(categoryId), "Test Category");
    Product product = new Product(new EntityId(productId), "Test Product", "Test Brand", category, "#FFFFFF");

    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Test Category");

    // Act
    ProductEntity result = productMapper.toEntity(product);

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getProductId());
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals("#FFFFFF", result.getColorHex());
    assertEquals(categoryId, result.getCategoryId());

  }

@Test
void toDomainList_shouldReturnEmptyListWhenInputIsNull() {
    // Act
    List<Product> result = productMapper.toDomainList(null);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
}

@Test
void toDomainList_shouldMapListOfProductEntities() {
    // Arrange
    ProductEntity entity1 = new ProductEntity();
    entity1.setProductId(UUID.randomUUID());
    entity1.setName("Product 1");
    entity1.setBrand("Brand 1");

    ProductEntity entity2 = new ProductEntity();
    entity2.setProductId(UUID.randomUUID());
    entity2.setName("Product 2");
    entity2.setBrand("Brand 2");

    // Act
    List<Product> result = productMapper.toDomainList(List.of(entity1, entity2));

    // Assert
    assertEquals(2, result.size());
    assertEquals(entity1.getName(), result.get(0).getName());
    assertEquals(entity2.getName(), result.get(1).getName());
}

@Test
void toEntityList_shouldReturnEmptyListWhenInputIsNull() {
    // Act
    List<ProductEntity> result = productMapper.toEntityList(null);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
}

@Test
void toProductUserRelation_shouldReturnNullWhenInputIsNull() {
    // Act
    ProductUserRelation result = productMapper.toProductUserRelation(null);

    // Assert
    assertNull(result);
}

@Test
void toReview_shouldReturnNullWhenInputIsNull() {
    // Act
    Review result = productMapper.toReview(null, new EntityId());

    // Assert
    assertNull(result);
}

@Test
void toReviewEntity_shouldReturnNullWhenInputIsNull() {
    // Act
    ReviewEntity result = productMapper.toReviewEntity(null, productUserId);

    // Assert
    assertNull(result);
}

@Test
void toReviewEntity_shouldMapReviewToReviewEntity() {
    // Arrange
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Review review = new Review(new EntityId(reviewId), new EntityId(userId), new EntityId(productUserId), 4, "Good product");
    
    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(userId);

    // Act
    ReviewEntity result = productMapper.toReviewEntity(review, productUserId);

    // Assert
    assertNotNull(result);
    assertEquals(reviewId, result.getReviewId());
    assertEquals(4, result.getRating());
    assertEquals("Good product", result.getComment());
    assertEquals(productUserId, result.getProductUserId());
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
    productEntity.setCategoryId(categoryId);

    ProductUserEntity productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setProductId(productId);
    productUserEntity.setUserId(UUID.randomUUID());

    ReviewEntity reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(UUID.randomUUID());
    reviewEntity.setRating(5);
    reviewEntity.setComment("Great product!");
    reviewEntity.setProductUserId(productUserId);

    productUserEntity.setReviews(List.of(reviewEntity));

    // Act
    Product result = productMapper.toDomainWithRelations(productEntity, List.of(productUserEntity));

    // Assert
    assertNotNull(result);
    assertEquals(productId, result.getId().getValue());
    assertEquals("Test Product", result.getName());
    assertEquals(1, result.getReviews().size());
    assertEquals(1, result.getUserRelations().size());

    Review review = result.getReviews().getFirst();
    assertEquals(reviewEntity.getReviewId(), review.getId().getValue());
    assertEquals(5, review.getRating());
    assertEquals("Great product!", review.getComment());
  }
}