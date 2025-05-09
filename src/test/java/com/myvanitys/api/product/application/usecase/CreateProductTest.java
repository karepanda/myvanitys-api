package com.myvanitys.api.product.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProductTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductUserRepository productUserRepository;

  @InjectMocks
  private CreateProduct createProduct;

  private CreateProductCommand command;

  private EntityId userId;

  private EntityId categoryId;

  private Category category;

  @BeforeEach
  void setUp() {
    userId = new EntityId(UUID.randomUUID());
    categoryId = new EntityId(UUID.randomUUID());

    command = new CreateProductCommand(
        "Test Product",
        "Test Brand",
        categoryId,
        "#FFFFFF",
        userId
    );

    category = new Category(categoryId, "Test Category");
  }

  @Test
  void shouldCreateNewProductWhenDoesNotExist() {
    // Arrange
    when(productRepository.findByName("Test Product", command.userId())).thenReturn(Optional.empty());
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

    // Usamos ArgumentCaptor para capturar el producto guardado
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

    // Configuramos el mock para devolver el mismo producto que se guardó
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Product result = createProduct.execute(command);

    // Assert
    assertNotNull(result);
    assertEquals("Test Product", result.getName());
    assertEquals("Test Brand", result.getBrand());
    assertEquals(category, result.getCategory());

    // Verificamos que se llamaron a los repositories correctamente
    verify(productRepository).findByName("Test Product", command.userId());
    verify(categoryRepository).findById(categoryId);
    verify(productRepository).save(productCaptor.capture());

    // Capturamos el producto que se guardó para verificarlo
    Product savedProduct = productCaptor.getValue();
    assertEquals("Test Product", savedProduct.getName());

    // Para la verificación de saveProductUserRelationship, usamos matchers para ambos parámetros
    verify(productUserRepository).saveProductUserRelationship(any(EntityId.class), eq(userId));
  }

  @Test
  void shouldReturnExistingProductWhenAlreadyExists() {
    // Arrange
    EntityId existingProductId = new EntityId(UUID.randomUUID());
    Product existingProduct = Product.reconstruct(
        existingProductId,
        "Test Product",
        "Existing Brand", // Diferente marca para verificar que se devuelve el existente
        category,
        "#000000", // Diferente color para verificar que se devuelve el existente
        null,       // Sin reviews
        null        // Sin relaciones usuario-producto
    );

    when(productRepository.findByName("Test Product", command.userId())).thenReturn(Optional.of(existingProduct));

    // Act
    Product result = createProduct.execute(command);

    // Assert
    assertNotNull(result);
    assertEquals(existingProductId, result.getId());
    assertEquals("Test Product", result.getName());
    assertEquals("Existing Brand", result.getBrand()); // Verificamos que es el producto existente

    // Verificar que NO se creó un nuevo producto
    verify(productRepository).findByName("Test Product", command.userId());
    verify(productRepository, never()).save(any(Product.class));

    // Verificar que se intentó crear la relación con el producto existente
    verify(productUserRepository).saveProductUserRelationship(existingProductId, userId);
  }

  @Test
  void shouldThrowExceptionWhenCategoryNotFound() {
    // Arrange
    when(productRepository.findByName("Test Product", command.userId())).thenReturn(Optional.empty());
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ValidationException.class, () -> createProduct.execute(command));

    // Verificar que no se intentó guardar el producto ni la relación
    verify(productRepository, never()).save(any(Product.class));
    verify(productUserRepository, never()).saveProductUserRelationship(any(), any());
  }
}