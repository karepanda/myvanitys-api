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
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
  private ProductMapper productMapper; // Add the mock for ProductMapper

  @InjectMocks
  private CreateProductUseCase useCase;

  @Captor
  private ArgumentCaptor<Product> productCaptor;

  private CreateProductCommand command;

  private ProductEntity mappedEntity;

  @BeforeEach
  void setUp() {
    // Setup test data
    EntityId productId = new EntityId();
    String name = "Test Product";
    String brand = "Test Brand";

    EntityId categoryId = new EntityId();
    Category category = new Category(categoryId, "Test Category");

    String colorHex = "#FF5733";

    command = new CreateProductCommand(productId, name, brand, category, colorHex);

    // Create a product entity to be returned by the mapper
    mappedEntity = new ProductEntity();
    // Set up any necessary fields for the entity
  }

  @Test
  void execute_ShouldSaveProductToRepository() {
    // Mock the mapper behavior to return our prepared entity
    when(productMapper.toEntity(any(Product.class))).thenReturn(mappedEntity);

    // When
    useCase.execute(command);

    // Then
    // Verify mapper was called with a product domain object
    verify(productMapper).toEntity(productCaptor.capture());

    // Get the captured domain object and verify its properties
    Product capturedProduct = productCaptor.getValue();
    assertThat(capturedProduct).satisfies(product -> {
      assertThat(product).isNotNull();
      assertThat(product.getName()).isEqualTo(command.name());
      assertThat(product.getBrand()).isEqualTo(command.brand());
      assertThat(product.getCategory()).isEqualTo(command.categoryID());
      assertThat(product.getColorHex()).isEqualTo(command.colorHex());
    });

    // Verify the repository was called with the mapped entity
    verify(productRepository).save(mappedEntity);
  }
}
