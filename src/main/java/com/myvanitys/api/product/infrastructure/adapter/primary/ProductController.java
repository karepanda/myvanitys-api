package com.myvanitys.api.product.infrastructure.adapter.primary;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import com.myvanitys.api.rest.v1.ProductsApiDelegate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@AllArgsConstructor
@Validated
public class ProductController implements ProductsApiDelegate {

  private final FindProductUserUseCase findProductUserUseCase;

  private final ProductResponseMapper productResponseMapper;

  @Override
  public ResponseEntity<ProductResponse> createProduct(UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent,
      @Valid CreateProductRequest createProductRequest) {

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

    // Wrap the raw UUID in a domain-specific identifier
    EntityId userIdValue = new EntityId(userId);

    // Create the query object
    FindProductUserQuery query = new FindProductUserQuery(userIdValue);

    // Execute the use case to fetch domain products
    List<Product> domainProducts = findProductUserUseCase.query(query);

    // Map domain products to API response objects
    List<ProductResponse> responseProducts = productResponseMapper.toResponseList(domainProducts);

    // Return the response with status 200 OK
    return ResponseEntity.ok(responseProducts);
  }

}