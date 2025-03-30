package com.myvanitys.api.product.infrastructure.adapter.primary;

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
}