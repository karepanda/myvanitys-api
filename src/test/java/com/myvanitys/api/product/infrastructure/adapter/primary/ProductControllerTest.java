package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductControllerTest {

  @InjectMocks
  private ProductController productController;

  @Mock
  private FindProductUserUseCase findProductUserUseCase;

  @Mock
  private ProductResponseMapper productResponseMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldCreateProductSuccessfully() {
    // Arrange
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    String acceptLanguage = "en-US";
    String userAgent = "JUnit Test";
    CreateProductRequest createProductRequest = new CreateProductRequest();
    createProductRequest.setName("Lipstick");
    createProductRequest.setBrand("MyBrand");
    createProductRequest.setColorHex("#FF5733");

    // Act
    ResponseEntity<ProductResponse> responseEntity = productController.createProduct(
        requestId,
        flowId,
        acceptLanguage,
        userAgent,
        createProductRequest
    );

    // Assert
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    ProductResponse responseBody = responseEntity.getBody();
    assertNotNull(responseBody);
    assertNotNull(responseBody.getId());
    assertEquals("Lipstick", responseBody.getName());
    assertEquals("MyBrand", responseBody.getBrand());
    assertEquals("#FF5733", responseBody.getColorHex());
  }
}