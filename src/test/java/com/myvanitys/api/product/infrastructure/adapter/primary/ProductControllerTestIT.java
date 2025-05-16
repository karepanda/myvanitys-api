package com.myvanitys.api.product.infrastructure.adapter.primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.model.v1.AddReviewRequest;
import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.application.usecase.AddReviewToProduct;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import com.myvanitys.api.product.infrastructure.adapter.primary.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTestIT extends AbstractIntegrationTest {

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public FindProductUserUseCase findProductUserUseCase() {
      return mock(FindProductUserUseCase.class);
    }

    @Bean
    @Primary
    public ProductResponseMapper productResponseMapper() {
      return mock(ProductResponseMapper.class);
    }

    @Bean
    @Primary
    public AddReviewToProduct addReviewToProduct() {return mock(AddReviewToProduct.class);}

    @Bean
    @Primary
    public TokenService tokenService() {
      return mock(TokenService.class);
    }
  }

  @BeforeEach
  void setUp() {
    reset(findProductUserUseCase, productResponseMapper);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FindProductUserUseCase findProductUserUseCase;

  @Autowired
  private ProductResponseMapper productResponseMapper;

  @Autowired
  private AddReviewToProduct addReviewToProduct;

  @Autowired
  private TokenService tokenService;

  // Constants for required headers
  private static final String ACCEPT_LANGUAGE = "en-US";

  private static final String USER_AGENT = "Mozilla/5.0 (Test)";

  @Test
  void shouldFindProductsByUserId() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String acceptLanguage = "en-US";
    String userAgent = "Mozilla/5.0 (Test)";

    // Print the userId to debug
    System.out.println("Test userId: " + userId);

    // Create domain model objects - Ensure entityId is properly constructed
    EntityId entityId = new EntityId(userId);
    System.out.println("EntityId value: " + entityId.getValue());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");
    Category category2 = new Category(new EntityId(UUID.randomUUID()), "Test Category2");

    // Crear relaciones de usuario para cada producto
    EntityId productId1 = new EntityId(UUID.randomUUID());
    EntityId productId2 = new EntityId(UUID.randomUUID());

    Set<ProductUserRelation> relations1 = new HashSet<>();
    relations1.add(ProductUserRelation.create(productId1, entityId));

    Set<ProductUserRelation> relations2 = new HashSet<>();
    relations2.add(ProductUserRelation.create(productId2, entityId));

    // Usar métodos de fábrica para crear productos
    List<Product> domainProducts = Arrays.asList(
        Product.reconstruct(
            productId1,
            "Product 1",
            "Brand 1",
            category,
            "#FF0000",
            new ArrayList<>(),  // Sin reviews
            relations1          // Con relación al usuario
        ),
        Product.reconstruct(
            productId2,
            "Product 2",
            "Brand 2",
            category2,
            "#00FF00",
            new ArrayList<>(),  // Sin reviews
            relations2          // Con relación al usuario
        )
    );

    // Create API response objects
    List<ProductResponse> responseProducts = Arrays.asList(
        new ProductResponse()
            .id(UUID.fromString(domainProducts.get(0).getId().getValue().toString()))
            .name(domainProducts.get(0).getName())
            .brand(domainProducts.get(0).getBrand())
            .colorHex(domainProducts.get(0).getColorHex()),
        new ProductResponse()
            .id(UUID.fromString(domainProducts.get(1).getId().getValue().toString()))
            .name(domainProducts.get(1).getName())
            .brand(domainProducts.get(1).getBrand())
            .colorHex(domainProducts.get(1).getColorHex())
    );

    // Configure mocks
    when(findProductUserUseCase.query(any(FindProductUserQuery.class))).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(responseProducts);
    when(tokenService.extractUserId(anyString())).thenReturn(userId);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", acceptLanguage)
            .header("User-Agent", userAgent)
            .header("Authorization", "Bearer 4/P7q7W91"))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name").value("Product 1"))
        .andExpect(jsonPath("$[0].brand").value("Brand 1"))
        .andExpect(jsonPath("$[0].colorHex").value("#FF0000"))
        .andExpect(jsonPath("$[1].name").value("Product 2"))
        .andExpect(jsonPath("$[1].brand").value("Brand 2"))
        .andExpect(jsonPath("$[1].colorHex").value("#00FF00"));

    // Simple verification
    verify(findProductUserUseCase).query(any(FindProductUserQuery.class));
  }

  @Test
  void shouldReturnBadRequestWhenHeadersAreMissing() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    CreateProductRequest request = new CreateProductRequest()
        .name("Test Product")
        .brand("Test Brand")
        .colorHex("#FF5733");

    // When/Then - Test create endpoint
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    // When/Then - Test find by user endpoint
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Disabled
  void shouldReturnBadRequestWhenCreateProductRequestIsInvalid() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Invalid request - missing required fields
    CreateProductRequest request = new CreateProductRequest();

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .header("User-Agent", USER_AGENT)
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnInternalServerErrorWhenFindProductsUserCaseFails() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // Configure mock to throw exception
    when(findProductUserUseCase.query(any(FindProductUserQuery.class)))
        .thenThrow(new RuntimeException("Internal service error"));
    when(tokenService.extractUserId(anyString())).thenReturn(userId);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .header("User-Agent", USER_AGENT)
            .header("Authorization", "Bearer 4/P7q7W91"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoProducts() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    // Configure mock to return empty list
    when(findProductUserUseCase.query(any(FindProductUserQuery.class)))
        .thenReturn(Collections.emptyList());
    when(productResponseMapper.toResponseList(anyList()))
        .thenReturn(Collections.emptyList());
    when(tokenService.extractUserId(anyString())).thenReturn(userId);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .header("User-Agent", USER_AGENT)
            .header("Authorization", "Bearer 4/P7q7W91"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void shouldAddReviewToProduct() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String acceptLanguage = "en-US";
    String userAgent = "Mozilla/5.0 (Test)";

    // Print the userId to debug
    System.out.println("Test userId: " + userId);

    // Create domain model objects - Ensure entityId is properly constructed
    EntityId entityId = new EntityId(userId);
    System.out.println("EntityId value: " + entityId.getValue());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

    // Crear relaciones de usuario para cada producto
    EntityId productId = new EntityId(UUID.randomUUID());
    EntityId reviewId = new EntityId(UUID.randomUUID());

    // Crear el request para la review
    AddReviewRequest request = new AddReviewRequest()
            .rating(5)
            .comment("Great product");


    Set<ProductUserRelation> relations = new HashSet<>();
    relations.add(ProductUserRelation.reconstruct(EntityId.newId(),entityId, productId, reviewId));

    Review review = Review.createWithExistingId(reviewId, entityId, ReviewDetails.create(5, "Great product"));
    List<Review> reviews = new ArrayList<>();
    reviews.add(review);

    Product updatedProduct = Product.reconstruct(
            productId,
            "Product 1",
            "Brand 1",
            category,
            "#FF0000",
            reviews,
            relations          // Con relación al usuario
    );

    // Create API response objects
    ProductResponse expectedResponse = new ProductResponse()
            .id(productId.getValue())
            .name("Product 1")
            .brand("Brand 1")
            .colorHex("#FF0000")
            .averageRating(5.0f);

    // Configure mocks
    when(tokenService.extractUserId(anyString())).thenReturn(userId);
    when(addReviewToProduct.execute(any(AddReviewToProductCommand.class))).thenReturn(updatedProduct);
    when(productResponseMapper.toResponse(updatedProduct)).thenReturn(expectedResponse);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/{productId}/reviews", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", acceptLanguage)
            .header("User-Agent", userAgent)
            .header("Authorization", "Bearer 4/P7q7W91")
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").value(productId.getValue().toString()))
        .andExpect(jsonPath("$.name").value("Product 1"))
        .andExpect(jsonPath("$.brand").value("Brand 1"))
        .andExpect(jsonPath("$.colorHex").value("#FF0000"))
        .andExpect(jsonPath("$.averageRating").value(5));


    // Verify that the mocks were called
    verify(addReviewToProduct).execute(any(AddReviewToProductCommand.class));
    verify(productResponseMapper).toResponse(updatedProduct);


  }

}