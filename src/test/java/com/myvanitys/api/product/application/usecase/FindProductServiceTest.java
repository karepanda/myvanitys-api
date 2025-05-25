package com.myvanitys.api.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.exception.CategoryNotFoundException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindProductServiceTest {

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private FindProductService target;

  @Nested
  class FindProducts {

    @Test
    void when_validProductEntities_then_returnsProducts() {
      // Arrange
      UUID productId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();

      EntityId categoryIdValue = new EntityId(categoryId);

      ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);

      Category category = new Category(categoryIdValue, "Test Category");
      List<Review> reviews = List.of();
      Product expectedProduct = Product.newProduct("Test Product", "Test Brand", "#000000");

      when(categoryRepository.findById(any(EntityId.class))).thenReturn(Optional.of(category));
      when(reviewRepository.findByProductId(any(EntityId.class))).thenReturn(reviews);
      when(productMapper.toDomain(productEntity, category, reviews)).thenReturn(expectedProduct);

      // Act
      List<Product> result = target.findProducts(List.of(productEntity));

      // Assert
      assertThat(result)
          .hasSize(1)
          .containsExactly(expectedProduct);
    }

    @Test
    void when_emptyProductList_then_returnEmptyList() {
      // Arrange
      List<ProductEntity> emptyList = Collections.emptyList();

      // Act & Assert
      List<Product> result = target.findProducts(emptyList);

      assertThat(result).isEmpty();
    }

    @Test
    void when_categoryNotFound_then_throwsCategoryNotFoundException() {
      // Arrange
      UUID productId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();

      ProductEntity productEntity = new ProductEntity();
      productEntity.setProductId(productId);
      productEntity.setCategoryId(categoryId);

      List<ProductEntity> productEntities = List.of(productEntity);

      when(categoryRepository.findById(any(EntityId.class))).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> target.findProducts(productEntities))
          .isInstanceOf(CategoryNotFoundException.class)
          .hasMessageContaining("Category not found for product: " + productId);
    }
  }
}