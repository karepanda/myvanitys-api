package com.myvanitys.api.product.infrastructure.adapter.primary;

import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductControllerTest {
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController();
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