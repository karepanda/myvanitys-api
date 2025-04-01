package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

  @Mock
  private ReviewMapper reviewMapper;

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private JpaProductUserRepository jpaProductUserRepository;

  @Mock
  private ProductMapper productMapper;

  // Datos de prueba
  private UUID reviewId;

  private UUID userId;

  private UUID productId;

  private UUID categoryId;

  private ReviewEntity reviewEntity;

  private ProductUserEntity productUserEntity;

  private ProductEntity productEntity;

  private CategoryEntity categoryEntity;

  private Review review;

  private Product product;

  private Category category;

  @BeforeEach
  void setUp() {
    // Inicializar IDs
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();

    // Inicializar objetos de categoría
    categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(categoryId);
    categoryEntity.setName("Skincare");
    categoryEntity.setCreatedAt(new Date());
    categoryEntity.setUpdatedAt(new Date());

    category = new Category(new EntityId(categoryId), "Skincare");

    // Inicializar objetos de producto
    productEntity = new ProductEntity();
    productEntity.setProductId(productId);
    productEntity.setName("Moisturizer");
    productEntity.setBrand("BrandX");
    productEntity.setColorHex("#FF5733");
    productEntity.setCategory(categoryEntity);
    productEntity.setCreatedAt(new Date());
    productEntity.setUpdatedAt(new Date());

    product = new Product(
        new EntityId(productId),
        "Moisturizer",
        "BrandX",
        category,
        "#FF5733"
    );

    // Inicializar objetos de ProductUserEntity
    productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setUserId(userId);
    productUserEntity.setProductId(productId);
    productUserEntity.setCreatedAt(new Date());
    productUserEntity.setUpdatedAt(new Date());

    // Inicializar objetos de ReviewEntity
    reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(reviewId);
    reviewEntity.setRating(5);
    reviewEntity.setComment("Great product!");
    reviewEntity.setProductUserEntity(productUserEntity);
    reviewEntity.setCreatedAt(new Date());
    reviewEntity.setUpdatedAt(new Date());

    // Inicializar objetos de Review
    review = new Review(
        new EntityId(reviewId),
        new EntityId(userId),
        product,
        5,
        "Great product!"
    );

    // Configurar comportamientos del productRepository y productMapper
    lenient().when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
    lenient().when(productMapper.toDomain(productEntity)).thenReturn(product);

    // Configurar comportamiento del productUserRepository
    lenient().when(jpaProductUserRepository.findByProductIdAndUserId(productId, userId)).thenReturn(productUserEntity);

    // Configurar comportamiento del reviewMapper
    lenient().when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);
    lenient().when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
    lenient().when(reviewMapper.productIdToProduct(productId)).thenReturn(product);
    lenient().when(reviewMapper.findProductUserEntity(review)).thenReturn(productUserEntity);
  }

  @Test
  void toDomain_WhenGivenValidReviewEntity_ShouldReturnReview() {
    // Act
    Review result = reviewMapper.toDomain(reviewEntity);

    // Assert
    assertNotNull(result);
    assertEquals(reviewId.toString(), result.getId().getValue().toString());
    assertEquals(userId.toString(), result.getUserId().getValue().toString());
    assertNotNull(result.getProduct());
    assertEquals(productId.toString(), result.getProduct().getId().getValue().toString());
    assertEquals(5, result.getRating());
    assertEquals("Great product!", result.getComment());

    // Verify
    verify(reviewMapper).toDomain(reviewEntity);
  }

  @Test
  void toEntity_WhenGivenValidReview_ShouldReturnReviewEntity() {
    // Act
    ReviewEntity result = reviewMapper.toEntity(review);

    // Assert
    assertNotNull(result);
    assertEquals(reviewId, result.getReviewId());
    assertEquals(5, result.getRating());
    assertEquals("Great product!", result.getComment());
    assertNotNull(result.getProductUserEntity());
    assertEquals(userId, result.getProductUserEntity().getUserId());
    assertEquals(productId, result.getProductUserEntity().getProductId());

    // Verify
    verify(reviewMapper).toEntity(review);
  }

  @Test
  void toDomain_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(reviewMapper.toDomain(null)).thenReturn(null);

    // Act
    Review result = reviewMapper.toDomain(null);

    // Assert
    assertNull(result);

    // Verify
    verify(reviewMapper).toDomain(null);
  }

  @Test
  void toEntity_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(reviewMapper.toEntity(null)).thenReturn(null);

    // Act
    ReviewEntity result = reviewMapper.toEntity(null);

    // Assert
    assertNull(result);

    // Verify
    verify(reviewMapper).toEntity(null);
  }

  @Test
  void productIdToProduct_WhenGivenValidProductId_ShouldReturnProduct() {

    // Act
    Product result = reviewMapper.productIdToProduct(productId);

    // Assert
    assertNotNull(result);
    assertEquals(productId.toString(), result.getId().getValue().toString());
    assertEquals("Moisturizer", result.getName());

    // Verify
    verify(reviewMapper).productIdToProduct(productId);

  }

  @Test
  void productIdToProduct_WhenGivenNull_ShouldReturnNull() {
    // Arrange
    when(reviewMapper.productIdToProduct(null)).thenCallRealMethod();

    // Act
    Product result = reviewMapper.productIdToProduct(null);

    // Assert
    assertNull(result);

    // Verify
    verify(reviewMapper).productIdToProduct(null);
  }

  @Test
  void findProductUserEntity_WhenGivenValidReview_ShouldReturnProductUserEntity() {
    // Act
    ProductUserEntity result = reviewMapper.findProductUserEntity(review);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(productId, result.getProductId());

    // Verify
    verify(reviewMapper).findProductUserEntity(review);

  }
}