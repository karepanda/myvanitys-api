package com.myvanitys.api.product.infrastructure.adapter.primary;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import com.myvanitys.api.product.infrastructure.adapter.primary.service.TokenService;
import com.myvanitys.api.product.infrastructure.exception.UnauthorizedException;
import com.myvanitys.api.rest.v1.ProductsApiDelegate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@AllArgsConstructor
public class ProductController implements ProductsApiDelegate {

  private final FindProductUserUseCase findProductUserUseCase;

  private final ProductResponseMapper productResponseMapper;

  private final CreateProductUseCase createProductUseCase;

  private final TokenService tokenService;

  @Override
  public ResponseEntity<ProductResponse> createProduct(UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent,
      CreateProductRequest createProductRequest) {

    // Extract the bearer token from the authorization header
    String bearerToken = extractBearerToken();

    // Get the userId from the token
    UUID userIdValue = tokenService.extractUserId(bearerToken);
    EntityId userId = new EntityId(userIdValue);

    // Create EntityId for categoryId
    EntityId categoryId = new EntityId(createProductRequest.getCategoryId());

    // Create command with all necessary data
    CreateProductCommand command = new CreateProductCommand(
        createProductRequest.getName(),
        createProductRequest.getBrand(),
        categoryId,
        createProductRequest.getColorHex(),
        userId
    );

    // Execute the use case
    Product createdProduct = createProductUseCase.execute(command);

    // Map the response
    ProductResponse response = productResponseMapper.toResponse(createdProduct);

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

  private String extractBearerToken() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      throw new UnauthorizedException("No request context available");
    }

    HttpServletRequest request = attributes.getRequest();
    String authorization = request.getHeader("Authorization");

    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }

    throw new UnauthorizedException("No bearer token found");
  }


}