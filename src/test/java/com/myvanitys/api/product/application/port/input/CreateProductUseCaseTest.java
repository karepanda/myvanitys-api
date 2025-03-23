package com.myvanitys.api.product.application.port.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.CategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CreateProductUseCase target;

  @Captor
  private ArgumentCaptor<Product> productCaptor;

  @Nested
  class Execute {

    private final EntityId productId = new EntityId();

    private final String name = "Test Product";

    private final String brand = "Test Brand";

    private final EntityId categoryId = new EntityId();

    private final Category category = new Category(categoryId, "Test Category");

    private final String colorHex = "#FF5733";

    private final EntityId userId = new EntityId();

    private final String reviewText = "Great product, highly recommended!";

    private CreateProductCommand command;

    private ProductEntity mappedEntity;

    private Product expectedProduct;

    @BeforeEach
    void setUp() {
      command = new CreateProductCommand(
          productId,
          name,
          brand,
          categoryId,
          colorHex,
          userId,
          reviewText
      );

      mappedEntity = new ProductEntity();

      expectedProduct = new Product(productId, name, brand, category, colorHex);

      when(productMapper.toEntity(any(Product.class))).thenReturn(mappedEntity);
      when(productRepository.save(any(ProductEntity.class))).thenReturn(mappedEntity);
      when(productMapper.toDomain(any(ProductEntity.class))).thenReturn(expectedProduct);
      when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));
    }

    @Test
    void when_validCommand_then_saveProductWithUserRelation() {
      // Act
      final Product result = target.execute(command);

      // Assert
      verify(productMapper).toEntity(productCaptor.capture());

      final Product capturedProduct = productCaptor.getValue();
      assertThat(capturedProduct.getId()).isEqualTo(productId);
      assertThat(capturedProduct.getName()).isEqualTo(name);
      assertThat(capturedProduct.getBrand()).isEqualTo(brand);
      assertThat(capturedProduct.getCategory()).isEqualTo(category);
      assertThat(capturedProduct.getColorHex()).isEqualTo(colorHex);

      verify(productRepository).save(mappedEntity);
      assertThat(result).isEqualTo(expectedProduct);
    }

    @Test
    void when_commandWithoutReviewText_then_saveProductWithUserRelationWithoutReview() {
      // Arrange
      final CreateProductCommand commandWithoutReview = new CreateProductCommand(
          productId, name, brand, categoryId, colorHex, userId, null
      );

      // Act
      final Product result = target.execute(commandWithoutReview);

      // Assert
      verify(productMapper).toEntity(productCaptor.capture());

      final Product capturedProduct = productCaptor.getValue();
      assertThat(capturedProduct.getId()).isEqualTo(productId);
      assertThat(capturedProduct.getName()).isEqualTo(name);
      assertThat(capturedProduct.getBrand()).isEqualTo(brand);
      assertThat(capturedProduct.getCategory()).isEqualTo(category);
      assertThat(capturedProduct.getColorHex()).isEqualTo(colorHex);

      verify(productRepository).save(mappedEntity);
      assertThat(result).isEqualTo(expectedProduct);
    }
  }
}