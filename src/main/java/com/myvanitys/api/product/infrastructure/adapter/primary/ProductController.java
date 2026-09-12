package com.myvanitys.api.product.infrastructure.adapter.primary;

import com.myvanitys.api.auth.infrastructure.security.AuthenticatedUserContext;
import com.myvanitys.api.model.v1.AddReviewRequest;
import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.model.v1.ProductSearchResponse;
import com.myvanitys.api.product.application.command.AddProductToMyVanityCommand;
import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.application.port.primary.*;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.application.usecase.AddReviewToProduct;
import com.myvanitys.api.product.application.usecase.FindProductByTerm;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import com.myvanitys.api.common.UnauthorizedException;
import com.myvanitys.api.rest.v1.ProductsApiDelegate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductController implements ProductsApiDelegate {

  private final FindProductUserUseCase findProductUserUseCase;

  private final FindProductAllUseCase findProductAllUseCase;

  private final ProductResponseMapper productResponseMapper;

  private final CreateProductUseCase createProductUseCase;

  private final AuthenticatedUserContext authenticatedUserContext;

  private final AddReviewToProduct addReviewToProduct;

  private final FindProductByTerm findProductByTerm;

  private final AddProductToMyVanityUseCase addProductToMyVanityUseCase;

  private final DeleteProductFromUserVanityUseCase deleteProductFromUserVanityUseCase;

  @Override
  public ResponseEntity<ProductResponse> createProduct(UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent,
      CreateProductRequest createProductRequest) {

    final EntityId userId = getUserId();

    EntityId categoryId = new EntityId(createProductRequest.getCategoryId());

    CreateProductCommand command = new CreateProductCommand(
        createProductRequest.getName(),
        createProductRequest.getBrand(),
        categoryId,
        createProductRequest.getColorHex(),
        userId
    );

    Product createdProduct = createProductUseCase.execute(command);

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

    final EntityId authenticatedUserId = getUserId();

    if (!authenticatedUserId.getValue().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    FindProductUserQuery query = new FindProductUserQuery(authenticatedUserId);
    List<Product> domainProducts = findProductUserUseCase.query(query);
    List<ProductResponse> responseProducts = productResponseMapper.toResponseList(domainProducts);

    return ResponseEntity.ok(responseProducts);
  }

  @Override
  public ResponseEntity<ProductSearchResponse> searchProducts(
      String query,
      UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent
  ) {

    final List<Product> products = findProductByTerm.query(query);

    List<ProductResponse> responseProducts = productResponseMapper.toResponseList(products);

    ProductSearchResponse productSearchResponse = new ProductSearchResponse().content(responseProducts);

    return ResponseEntity.ok(productSearchResponse);
  }

  @Override
  public ResponseEntity<ProductResponse> addReviewToProduct(UUID productId,
      UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent,
      AddReviewRequest addReviewRequest) {

    final EntityId userId = getUserId();
    EntityId productEntityId = new EntityId(productId);
    AddReviewToProductCommand command = new AddReviewToProductCommand(
        userId,
        productEntityId,
        addReviewRequest.getRating(),
        addReviewRequest.getComment(),
        Instant.now()
    );

    Product updatedProduct = addReviewToProduct.execute(command);

    ProductResponse response = productResponseMapper.toResponse(updatedProduct);

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @Override
  public ResponseEntity<ProductResponse> addProductToUserVanity(
      UUID productId,
      UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent) {

    AddProductToMyVanityCommand command = new AddProductToMyVanityCommand(productId, getUserId().getValue());

    Product addedProduct = addProductToMyVanityUseCase.execute(command);

    ProductResponse response = productResponseMapper.toResponse(addedProduct);


    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<ProductResponse>> getAllProductsWithCollectionStatus(
      UUID xRequestID,
      UUID xFlowID,
      String acceptLanguage,
      String userAgent) {

    List<Product> domainProducts = findProductAllUseCase.query();
    List<ProductResponse> responseProducts = productResponseMapper.toResponseList(domainProducts);

    return ResponseEntity.ok(responseProducts);
  }

  @Override
  public ResponseEntity<Void> deleteProductByUser(
          UUID productId,
          UUID xRequestID,
          UUID xFlowID,
          String acceptLanguage,
          String userAgent) {

    DeleteProductFromUserVanityCommand command = new DeleteProductFromUserVanityCommand(productId, getUserId().getValue());

    deleteProductFromUserVanityUseCase.execute(command);

    return ResponseEntity.noContent().build();
  }

  private EntityId getUserId() {
    UUID userIdValue = authenticatedUserContext.getUserId();
    if (userIdValue == null) {
      throw new UnauthorizedException("No authenticated user in request context");
    }
    return new EntityId(userIdValue);
  }


}