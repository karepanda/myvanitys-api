package com.myvanitys.api.product.infrastructure.adapter.primary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.rest.v1.ProductsApiDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductController implements ProductsApiDelegate {

  @Override
  public ResponseEntity<ProductResponse> createProduct(UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent,
      CreateProductRequest createProductRequest) {

    ProductResponse response = new ProductResponse();
    response.setId(UUID.randomUUID());
    response.setName(createProductRequest.getName());
    response.setBrand(createProductRequest.getBrand());
    response.setColorHex(createProductRequest.getColorHex());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<List<ProductResponse>> findProductsByUserId(
      UUID userId,
      UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent) {

    // Here you would typically call a service to retrieve products by user ID
    // For demonstration purposes, I'll create some sample data
    List<ProductResponse> userProducts = new ArrayList<>();

    // Sample product 1
    ProductResponse product1 = new ProductResponse();
    product1.setId(UUID.randomUUID());
    product1.setName("Sample Product 1");
    product1.setBrand("Brand A");
    product1.setColorHex("#FF5733");
    userProducts.add(product1);

    // Sample product 2
    ProductResponse product2 = new ProductResponse();
    product2.setId(UUID.randomUUID());
    product2.setName("Sample Product 2");
    product2.setBrand("Brand B");
    product2.setColorHex("#33FF57");
    userProducts.add(product2);

    // In a real implementation, you would query a repository:
    // List<ProductResponse> userProducts = productService.findByUserId(userId);

    return ResponseEntity.ok(userProducts);
  }
}