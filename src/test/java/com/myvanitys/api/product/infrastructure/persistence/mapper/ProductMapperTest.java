package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
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

  private UUID categoryId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();
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
    reviewEntity.setProductUserEntity(productUserEntity);

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
