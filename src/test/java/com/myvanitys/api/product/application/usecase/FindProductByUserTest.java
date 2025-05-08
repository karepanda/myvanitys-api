package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindProductByUserTest {

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ReviewRepository reviewRepository;
  @InjectMocks
  private FindProductByUser target;

  @Nested
  class Query {
 
    @Test
    void when_userHasProducts_then_returnMappedProducts() {
      // Create a random user ID and query for finding products
      // This simulates a request to find all products for a specific user
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);

      // Create two product entities with specific IDs
      // These represent the products stored in the database
      final ProductEntity productId1 = createProductEntity("11111111-1111-1111-1111-111111111111");
      final ProductEntity productId2 = createProductEntity("22222222-2222-2222-2222-222222222222");

      // Create categories for the products
      final EntityId categoryId1 = new EntityId(productId1.getCategoryId());
      final EntityId categoryId2 = new EntityId(productId2.getCategoryId());
      final Category category1 = new Category(categoryId1, "Makeup");
      final Category category2 = new Category(categoryId2, "Eye Makeup");

      // Create sample reviews for each product
      final List<Review> reviews1 = List.of(
              Review.createWithExistingId(
                      EntityId.newId(),
                      EntityId.newId(),
                      ReviewDetails.create(5, "Great product!"))
      );

      final List<Review> reviews2 = List.of(
              Review.createWithExistingId(
                      EntityId.newId(),
                      EntityId.newId(),
                      ReviewDetails.create(4, "Great product!"))
      );

      // Create the expected domain objects
      // These represent what we expect the service to return
      final Product domain1 = Product.reconstruct(
        new EntityId(UUID.fromString("765d310a-c838-429c-bd82-3172c2d7f49e")),
        "Lipstick", "Maybelline", category1, "#FF0000",
        reviews1,
        null
      );

      final Product domain2 = Product.reconstruct(
        new EntityId(UUID.fromString("cc2217f0-56c7-48f8-beb3-8da8f6f70f23")),
        "Mascara", "L'Oreal", category2, "#000000",
        reviews2,
        null
      );

      // Configure mock behaviors
      // Define what the repositories should return when called
      when(jpaProductRepository.findByUserId(userId.getValue()))
        .thenReturn(List.of(productId1, productId2));

      when(categoryRepository.findById(eq(new EntityId(productId1.getCategoryId()))))
        .thenReturn(Optional.of(category1));
      when(categoryRepository.findById(eq(new EntityId(productId2.getCategoryId()))))
        .thenReturn(Optional.of(category2));

      when(reviewRepository.findByProductId(any(EntityId.class)))
        .thenReturn(reviews1)
        .thenReturn(reviews2);

      // Configure the ProductMapper behavior
      // Define how entities should be mapped to domain objects
      doReturn(domain1)
        .when(productMapper)
        .toDomain(eq(productId1), eq(category1), eq(reviews1));

      doReturn(domain2)
        .when(productMapper)
        .toDomain(eq(productId2), eq(category2), eq(reviews2));

      // Execute the method being tested
      final List<Product> result = target.query(query);

      // Verify the results
      // Check that we got the expected products back
      assertThat(result)
        .hasSize(2)
        .containsExactlyInAnyOrder(domain1, domain2);

      // Verify that all expected interactions occurred
      // Ensure that our repositories and mappers were called correctly
      verify(categoryRepository).findById(eq(new EntityId(productId1.getCategoryId())));
      verify(categoryRepository).findById(eq(new EntityId(productId2.getCategoryId())));
      verify(reviewRepository, times(2)).findByProductId(any(EntityId.class));
      verify(productMapper).toDomain(eq(productId1), eq(category1), eq(reviews1));
      verify(productMapper).toDomain(eq(productId2), eq(category2), eq(reviews2));
    }

    // Helper method to create product entities for testing
    private ProductEntity createProductEntity(String id) {
      UUID categoryId = UUID.randomUUID();
      ProductEntity entity = new ProductEntity();
      entity.setProductId(UUID.fromString(id));
      entity.setCategoryId(categoryId);
      entity.setName("Test Product");
      entity.setBrand("Test Brand");
      entity.setColorHex("#FF0000");
      return entity;
    }

    @Test
    void when_userHasNoProducts_then_throwProductNotFoundException() {
      final EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);

      when(jpaProductRepository.findByUserId(userId.getValue())).thenReturn(List.of());

      assertThrows(ProductNotFoundException.class, () -> target.query(query));
    }

    @Test
    void verify_mockInteractions_whenProductsAreFound() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");
      final Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");
      final List<Review> reviews = List.of();

      final Product domainProduct = Product.reconstruct(
          new EntityId(UUID.randomUUID()),
          "Test Product",
          "Test Brand",
          category,
          "#000000",
          reviews,
          null
      );

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(categoryRepository.findById(eq(new EntityId(productEntity.getCategoryId()))))
              .thenReturn(Optional.of(category));
      when(productMapper.toDomain(eq(productEntity), any(Category.class), eq(reviews))).thenReturn(domainProduct);

      // Act
      target.query(query);

      // Assert
      verify(jpaProductRepository, times(1)).findByUserId(userId.getValue());
      verify(categoryRepository, times(1)).findById(any(EntityId.class));
      verify(productMapper, times(1)).toDomain(eq(productEntity), any(Category.class), eq(reviews));
      verifyNoMoreInteractions(jpaProductRepository, productMapper);
    }

    @Test
    void verify_productProperties_whenProductIsFound() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");

      final EntityId categoryId = new EntityId(UUID.randomUUID());
      final Category category = new Category(categoryId, "Test Category");

      final Product expectedProduct = Product.reconstruct(
          new EntityId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
          "Test Product",
          "Test Brand",
          category,
          "#FF0000",
          null,
          null
      );

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(categoryRepository.findById(any(EntityId.class)))
          .thenReturn(Optional.of(category));
      when(productMapper.toDomain(eq(productEntity), any(Category.class), any())).thenReturn(expectedProduct);

      // Act
      List<Product> result = target.query(query);

      // Assert
      assertThat(result).hasSize(1);
      Product actualProduct = result.getFirst();
      assertAll(
          () -> assertEquals(expectedProduct.getName(), actualProduct.getName()),
          () -> assertEquals(expectedProduct.getBrand(), actualProduct.getBrand()),
          () -> assertEquals(expectedProduct.getCategory(), actualProduct.getCategory()),
          () -> assertEquals(expectedProduct.getColorHex(), actualProduct.getColorHex())
      );
    }

    @Test
    void verify_exceptionIsThrown_whenProductMapperReturnsNull() {
      // Arrange
      EntityId userId = new EntityId(UUID.randomUUID());
      final FindProductUserQuery query = new FindProductUserQuery(userId);
      final ProductEntity productEntity = createProductEntity("11111111-1111-1111-1111-111111111111");
      final Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

      when(jpaProductRepository.findByUserId(userId.getValue()))
          .thenReturn(List.of(productEntity));
      when(categoryRepository.findById(any(EntityId.class)))
          .thenReturn(Optional.of(category));
      when(productMapper.toDomain(eq(productEntity), any(Category.class), any())).thenReturn(null);

      // Act & Assert
      assertThrows(ProductNotFoundException.class, () -> target.query(query));
    }

  }
}