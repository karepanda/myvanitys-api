package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.security.AuthenticatedUserContext;
import com.myvanitys.api.common.UnauthorizedException;
import com.myvanitys.api.model.v1.AddReviewRequest;
import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.model.v1.ProductSearchResponse;
import com.myvanitys.api.product.application.command.AddProductToMyVanityCommand;
import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.application.port.primary.AddProductToMyVanityUseCase;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.application.port.primary.DeleteProductFromUserVanityUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductAllUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.usecase.AddReviewToProduct;
import com.myvanitys.api.product.application.usecase.FindProductByTerm;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductControllerTest {

  private static final UUID REQUEST_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
  private static final UUID FLOW_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
  private static final UUID AUTHENTICATED_USER_ID = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001");
  private static final UUID PRODUCT_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
  private static final UUID OTHER_PRODUCT_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000002");
  private static final UUID CREATED_PRODUCT_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
  private static final UUID CATEGORY_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
  private static final String ACCEPT_LANGUAGE = "en-US";
  private static final String USER_AGENT = "Mozilla/5.0 (Test)";

  private FindProductUserUseCase findProductUserUseCase;
  private FindProductAllUseCase findProductAllUseCase;
  private ProductResponseMapper productResponseMapper;
  private CreateProductUseCase createProductUseCase;
  private AuthenticatedUserContext context;
  private AddReviewToProduct addReviewToProduct;
  private FindProductByTerm findProductByTerm;
  private AddProductToMyVanityUseCase addProductToMyVanityUseCase;
  private DeleteProductFromUserVanityUseCase deleteProductFromUserVanityUseCase;
  private ProductController controller;

  @BeforeEach
  void setUp() {
    findProductUserUseCase = mock(FindProductUserUseCase.class);
    findProductAllUseCase = mock(FindProductAllUseCase.class);
    productResponseMapper = mock(ProductResponseMapper.class);
    createProductUseCase = mock(CreateProductUseCase.class);
    context = new AuthenticatedUserContext();
    addReviewToProduct = mock(AddReviewToProduct.class);
    findProductByTerm = mock(FindProductByTerm.class);
    addProductToMyVanityUseCase = mock(AddProductToMyVanityUseCase.class);
    deleteProductFromUserVanityUseCase = mock(DeleteProductFromUserVanityUseCase.class);
    controller = new ProductController(
        findProductUserUseCase,
        findProductAllUseCase,
        productResponseMapper,
        createProductUseCase,
        context,
        addReviewToProduct,
        findProductByTerm,
        addProductToMyVanityUseCase,
        deleteProductFromUserVanityUseCase
    );
  }

  @Test
  void shouldReturn403WhenPathUserIdDoesNotMatchAuthenticatedUser() {
    UUID authenticatedUserId = UUID.randomUUID();
    UUID pathUserId = UUID.randomUUID();
    context.setUserId(authenticatedUserId);

    ResponseEntity<List<ProductResponse>> response = controller.findProductsByUserId(
        pathUserId, UUID.randomUUID(), UUID.randomUUID(), "en-US", "Mozilla/5.0");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(findProductUserUseCase, never()).query(any());
  }

  @Test
  void shouldReturn200WhenPathUserIdMatchesAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    context.setUserId(userId);

    when(findProductUserUseCase.query(any())).thenReturn(List.of());
    when(productResponseMapper.toResponseList(anyList())).thenReturn(List.of());

    ResponseEntity<List<ProductResponse>> response = controller.findProductsByUserId(
        userId, UUID.randomUUID(), UUID.randomUUID(), "en-US", "Mozilla/5.0");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(findProductUserUseCase).query(org.mockito.ArgumentMatchers.argThat(
        query -> query.userId().getValue().equals(userId)));
  }

  @Test
  void createProduct_shouldComposeCommandFromRequestAndAuthenticatedUser() {
    // Given
    context.setUserId(AUTHENTICATED_USER_ID);
    CreateProductRequest request = new CreateProductRequest()
        .name("Test Product")
        .brand("Test Brand")
        .categoryId(CATEGORY_ID)
        .colorHex("#FF5733");
    Product createdProduct = mock(Product.class);
    ProductResponse expectedResponse = new ProductResponse()
        .id(CREATED_PRODUCT_ID)
        .name("Test Product")
        .brand("Test Brand")
        .colorHex("#FF5733");

    when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(createdProduct);
    when(productResponseMapper.toResponse(createdProduct)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ProductResponse> response = controller.createProduct(
        REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT, request);

    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertSame(expectedResponse, response.getBody());

    ArgumentCaptor<CreateProductCommand> commandCaptor = ArgumentCaptor.forClass(CreateProductCommand.class);
    verify(createProductUseCase).execute(commandCaptor.capture());
    CreateProductCommand command = commandCaptor.getValue();
    assertEquals("Test Product", command.name());
    assertEquals("Test Brand", command.brand());
    assertEquals(CATEGORY_ID, command.categoryId().getValue());
    assertEquals("#FF5733", command.colorHex());
    assertEquals(AUTHENTICATED_USER_ID, command.userId().getValue());

    verify(productResponseMapper).toResponse(createdProduct);
  }

  @Test
  void searchProducts_shouldReturnMappedListInOrderWithoutAuthenticatedUser() {
    // Given
    String searchTerm = "makeup";
    Product firstProduct = mock(Product.class);
    Product secondProduct = mock(Product.class);
    List<Product> domainProducts = List.of(firstProduct, secondProduct);
    ProductResponse firstResponse = new ProductResponse()
        .id(PRODUCT_ID)
        .name("Makeup palette");
    ProductResponse secondResponse = new ProductResponse()
        .id(OTHER_PRODUCT_ID)
        .name("Lipstick");
    List<ProductResponse> mappedResponses = List.of(firstResponse, secondResponse);

    when(findProductByTerm.query(searchTerm)).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(mappedResponses);

    // When
    ResponseEntity<ProductSearchResponse> response = controller.searchProducts(
        searchTerm, REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertSame(mappedResponses, response.getBody().getContent());
    verify(findProductByTerm).query(searchTerm);
    verify(productResponseMapper).toResponseList(domainProducts);
  }

  @Test
  void addReviewToProduct_shouldComposeCommandWithTimestamps() {
    // Given
    context.setUserId(AUTHENTICATED_USER_ID);
    AddReviewRequest request = new AddReviewRequest()
        .rating(4)
        .comment("Great product");
    Product updatedProduct = mock(Product.class);
    ProductResponse expectedResponse = new ProductResponse()
        .id(PRODUCT_ID)
        .name("Updated product");

    when(addReviewToProduct.execute(any(AddReviewToProductCommand.class))).thenReturn(updatedProduct);
    when(productResponseMapper.toResponse(updatedProduct)).thenReturn(expectedResponse);

    // When
    Instant before = Instant.now();
    ResponseEntity<ProductResponse> response = controller.addReviewToProduct(
        PRODUCT_ID, REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT, request);
    Instant after = Instant.now();

    // Then
    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertSame(expectedResponse, response.getBody());

    ArgumentCaptor<AddReviewToProductCommand> commandCaptor = ArgumentCaptor.forClass(AddReviewToProductCommand.class);
    verify(addReviewToProduct).execute(commandCaptor.capture());
    AddReviewToProductCommand command = commandCaptor.getValue();
    assertEquals(AUTHENTICATED_USER_ID, command.userId().getValue());
    assertEquals(PRODUCT_ID, command.productId().getValue());
    assertEquals(4, command.reviewDetails().rating());
    assertEquals("Great product", command.reviewDetails().comment());

    Instant createdAt = command.reviewDetails().createdAt().asInstant();
    assertNotNull(createdAt);
    assertFalse(createdAt.isBefore(before));
    assertFalse(createdAt.isAfter(after));
    assertEquals(createdAt, command.reviewDetails().updatedAt().asInstant());
    assertNull(command.reviewDetails().deletedAt());

    verify(productResponseMapper).toResponse(updatedProduct);
  }

  @Test
  void addProductToUserVanity_shouldComposeCommandFromPathAndAuthenticatedUser() {
    // Given
    context.setUserId(AUTHENTICATED_USER_ID);
    Product addedProduct = mock(Product.class);
    ProductResponse expectedResponse = new ProductResponse()
        .id(PRODUCT_ID)
        .name("Added product");

    when(addProductToMyVanityUseCase.execute(any(AddProductToMyVanityCommand.class))).thenReturn(addedProduct);
    when(productResponseMapper.toResponse(addedProduct)).thenReturn(expectedResponse);

    // When
    ResponseEntity<ProductResponse> response = controller.addProductToUserVanity(
        PRODUCT_ID, REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expectedResponse, response.getBody());

    ArgumentCaptor<AddProductToMyVanityCommand> commandCaptor = ArgumentCaptor.forClass(AddProductToMyVanityCommand.class);
    verify(addProductToMyVanityUseCase).execute(commandCaptor.capture());
    assertEquals(PRODUCT_ID, commandCaptor.getValue().productId());
    assertEquals(AUTHENTICATED_USER_ID, commandCaptor.getValue().userId());

    verify(productResponseMapper).toResponse(addedProduct);
  }

  @Test
  void getAllProductsWithCollectionStatus_shouldReturnMappedListWithoutAuthenticatedUser() {
    // Given
    Product firstProduct = mock(Product.class);
    Product secondProduct = mock(Product.class);
    List<Product> domainProducts = List.of(firstProduct, secondProduct);
    ProductResponse firstResponse = new ProductResponse()
        .id(PRODUCT_ID)
        .name("Product 1");
    ProductResponse secondResponse = new ProductResponse()
        .id(OTHER_PRODUCT_ID)
        .name("Product 2");
    List<ProductResponse> mappedResponses = List.of(firstResponse, secondResponse);

    when(findProductAllUseCase.query()).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(mappedResponses);

    // When
    ResponseEntity<List<ProductResponse>> response = controller.getAllProductsWithCollectionStatus(
        REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(mappedResponses, response.getBody());
    verify(findProductAllUseCase, times(1)).query();
    verify(productResponseMapper).toResponseList(domainProducts);
  }

  @Test
  void deleteProductByUser_shouldComposeCommandAndReturnNoContent() {
    // Given
    context.setUserId(AUTHENTICATED_USER_ID);

    // When
    ResponseEntity<Void> response = controller.deleteProductByUser(
        PRODUCT_ID, REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT);

    // Then
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());

    ArgumentCaptor<DeleteProductFromUserVanityCommand> commandCaptor =
        ArgumentCaptor.forClass(DeleteProductFromUserVanityCommand.class);
    verify(deleteProductFromUserVanityUseCase, times(1)).execute(commandCaptor.capture());
    assertEquals(PRODUCT_ID, commandCaptor.getValue().productId());
    assertEquals(AUTHENTICATED_USER_ID, commandCaptor.getValue().userId());
  }

  @Test
  void createProduct_shouldThrowUnauthorizedWhenNoAuthenticatedUser() {
    // Given
    CreateProductRequest request = new CreateProductRequest()
        .name("Test Product")
        .brand("Test Brand")
        .categoryId(CATEGORY_ID)
        .colorHex("#FF5733");

    // When
    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> controller.createProduct(REQUEST_ID, FLOW_ID, ACCEPT_LANGUAGE, USER_AGENT, request));

    // Then
    assertEquals("No authenticated user in request context", exception.getMessage());
    verify(createProductUseCase, never()).execute(any(CreateProductCommand.class));
    verify(productResponseMapper, never()).toResponse(any(Product.class));
  }
}
