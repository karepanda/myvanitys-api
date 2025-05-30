package com.myvanitys.api.product.infrastructure.adapter.primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.model.v1.AddReviewRequest;
import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductAllUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.application.usecase.AddReviewToProduct;
import com.myvanitys.api.product.application.usecase.FindProductByTerm;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
import com.myvanitys.api.product.infrastructure.adapter.primary.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
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
    public CreateProductUseCase createProductUseCase() {
      return mock(CreateProductUseCase.class);
    }

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
    public AddReviewToProduct addReviewToProduct() {
      return mock(AddReviewToProduct.class);
    }

    @Bean
    @Primary
    public TokenService tokenService() {
      return mock(TokenService.class);
    }

    @Bean
    @Primary
    public FindProductByTerm findProductByTerm() {
      return mock(FindProductByTerm.class);
    }

    @Bean
    @Primary
    public FindProductAllUseCase findProductAllUseCase() {
      return mock(FindProductAllUseCase.class);
    }

  }

  @BeforeEach
  void setUp() {
    reset(findProductUserUseCase, productResponseMapper, findProductByTerm, findProductAllUseCase, createProductUseCase, tokenService, addReviewToProduct);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CreateProductUseCase createProductUseCase;

  @Autowired
  private FindProductUserUseCase findProductUserUseCase;

  @Autowired
  private ProductResponseMapper productResponseMapper;

  @Autowired
  private AddReviewToProduct addReviewToProduct;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private FindProductByTerm findProductByTerm;

  @Autowired
  private FindProductAllUseCase findProductAllUseCase;

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

    // Create user relations for each product
    EntityId productId1 = new EntityId(UUID.randomUUID());
    EntityId productId2 = new EntityId(UUID.randomUUID());

    Set<ProductUserRelation> relations1 = new HashSet<>();
    relations1.add(ProductUserRelation.create(productId1, entityId));

    Set<ProductUserRelation> relations2 = new HashSet<>();
    relations2.add(ProductUserRelation.create(productId2, entityId));

    // Use factory methods to create products
    List<Product> domainProducts = Arrays.asList(
            Product.reconstruct(
                    productId1,
                    "Product 1",
                    "Brand 1",
                    category,
                    "#FF0000",
                    new ArrayList<>(),
                    relations1
            ),
            Product.reconstruct(
                    productId2,
                    "Product 2",
                    "Brand 2",
                    category2,
                    "#00FF00",
                    new ArrayList<>(),
                    relations2
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

    // Configure mock to return an empty list
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

    EntityId productId = new EntityId(UUID.randomUUID());
    EntityId reviewId = new EntityId(UUID.randomUUID());

    AddReviewRequest request = new AddReviewRequest()
            .rating(5)
            .comment("Great product");

    Set<ProductUserRelation> relations = new HashSet<>();
    relations.add(ProductUserRelation.reconstruct(EntityId.newId(), entityId, productId, reviewId));

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
            relations
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

  @Test
  void shouldReturnProductsBySearchTerm() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    final String searchTerm = "makeup";

    // Create domain objects
    EntityId productId1 = new EntityId(UUID.randomUUID());
    EntityId productId2 = new EntityId(UUID.randomUUID());
    Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

    List<Product> domainProducts = List.of(
            Product.reconstruct(
                    productId1,
                    "Makeup palette",
                    "Brand X",
                    category,
                    "#FF0000",
                    new ArrayList<>(),
                    new HashSet<>()
            ),
            Product.reconstruct(
                    productId2,
                    "Lipstick",
                    "Makeup Brand",
                    category,
                    "#00FF00",
                    new ArrayList<>(),
                    new HashSet<>()
            )
    );

    List<ProductResponse> responseProducts = List.of(
            new ProductResponse()
                    .id(productId1.getValue())
                    .name("Makeup palette")
                    .brand("Brand X")
                    .colorHex("#FF0000")
                    .averageRating(0.0f),
            new ProductResponse()
                    .id(productId2.getValue())
                    .name("Lipstick")
                    .brand("Makeup Brand")
                    .colorHex("#00FF00")
                    .averageRating(0.0f)
    );

    // Configure mocks
    when(findProductByTerm.query(searchTerm)).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(responseProducts);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/search")
                    .param("query", searchTerm)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name").value("Makeup palette"))
            .andExpect(jsonPath("$.content[0].brand").value("Brand X"))
            .andExpect(jsonPath("$.content[1].name").value("Lipstick"))
            .andExpect(jsonPath("$.content[1].brand").value("Makeup Brand"));

    // Verify that the mocks were called
    verify(findProductByTerm).query(searchTerm);
    verify(productResponseMapper).toResponseList(domainProducts);
    verifyNoMoreInteractions(findProductByTerm, productResponseMapper);
  }

  @Test
  void whenProductsBySearchTermShouldReturnEmptyList() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    final String searchTerm = "Makeup";

    // Create domain objects
    EntityId productId1 = new EntityId(UUID.randomUUID());
    EntityId productId2 = new EntityId(UUID.randomUUID());
    Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

    List<Product> domainProducts = List.of(
            Product.reconstruct(
                    productId1,
                    "name1",
                    "Brand1",
                    category,
                    "#FF0000",
                    new ArrayList<>(),
                    new HashSet<>()
            ),
            Product.reconstruct(
                    productId2,
                    "name2",
                    "Brand2",
                    category,
                    "#00FF00",
                    new ArrayList<>(),
                    new HashSet<>()
            )
    );

    List<ProductResponse> responseProducts = List.of(
            new ProductResponse()
                    .id(productId1.getValue())
                    .name("Makeup palette")
                    .brand("Brand X")
                    .colorHex("#FF0000")
                    .averageRating(0.0f),
            new ProductResponse()
                    .id(productId2.getValue())
                    .name("Lipstick")
                    .brand("Makeup Brand")
                    .colorHex("#00FF00")
                    .averageRating(0.0f)
    );

    // Configure mocks
    when(findProductByTerm.query(searchTerm)).thenReturn(Collections.emptyList());
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(responseProducts);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/search")
                    .param("query", searchTerm)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));

    // Verify that the mocks were called
    verify(findProductByTerm).query(searchTerm);
    verify(productResponseMapper).toResponseList(Collections.emptyList());
    verifyNoMoreInteractions(findProductByTerm, productResponseMapper);

  }

  @Test
  void shouldGetAllProductsWithCollectionStatus() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Create domain objects
    EntityId productId1 = new EntityId(UUID.randomUUID());
    EntityId productId2 = new EntityId(UUID.randomUUID());
    Category category1 = new Category(new EntityId(UUID.randomUUID()), "Category 1");
    Category category2 = new Category(new EntityId(UUID.randomUUID()), "Category 2");

    List<Product> domainProducts = List.of(
            Product.reconstruct(
                    productId1,
                    "Product 1",
                    "Brand 1",
                    category1,
                    "#FF0000",
                    new ArrayList<>(),
                    new HashSet<>()
            ),
            Product.reconstruct(
                    productId2,
                    "Product 2",
                    "Brand 2",
                    category2,
                    "#00FF00",
                    new ArrayList<>(),
                    new HashSet<>()
            )
    );

    List<ProductResponse> responseProducts = List.of(
            new ProductResponse()
                    .id(productId1.getValue())
                    .name("Product 1")
                    .brand("Brand 1")
                    .colorHex("#FF0000")
                    .averageRating(0.0f),
            new ProductResponse()
                    .id(productId2.getValue())
                    .name("Product 2")
                    .brand("Brand 2")
                    .colorHex("#00FF00")
                    .averageRating(0.0f)
    );

    // Configure mocks
    when(findProductAllUseCase.query()).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(responseProducts);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(productId1.getValue().toString()))
            .andExpect(jsonPath("$[0].name").value("Product 1"))
            .andExpect(jsonPath("$[0].brand").value("Brand 1"))
            .andExpect(jsonPath("$[0].colorHex").value("#FF0000"))
            .andExpect(jsonPath("$[1].id").value(productId2.getValue().toString()))
            .andExpect(jsonPath("$[1].name").value("Product 2"))
            .andExpect(jsonPath("$[1].brand").value("Brand 2"))
            .andExpect(jsonPath("$[1].colorHex").value("#00FF00"));

    // Verify that the mocks were called
    verify(findProductAllUseCase).query();
    verify(productResponseMapper).toResponseList(domainProducts);
    verifyNoMoreInteractions(findProductAllUseCase, productResponseMapper);
  }

  @Test
  void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Configure mocks to return emptyList
    when(findProductAllUseCase.query()).thenReturn(Collections.emptyList());
    when(productResponseMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

    // Verify that the mocks were called
    verify(findProductAllUseCase).query();
    verify(productResponseMapper).toResponseList(Collections.emptyList());
    verifyNoMoreInteractions(findProductAllUseCase, productResponseMapper);
  }

  @Test
  void shouldReturnInternalServerErrorWhenFindAllProductsUseCaseFails() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();

    // Configure mock to throw exception
    when(findProductAllUseCase.query()).thenThrow(new RuntimeException("Database connection error"));

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT))
            .andExpect(status().isInternalServerError());

    // Verify that the mock was called
    verify(findProductAllUseCase).query();
    verifyNoMoreInteractions(findProductAllUseCase, productResponseMapper);
  }

  @Test
  void shouldReturnBadRequestWhenRequiredHeadersAreMissingForGetAllProducts() throws Exception {
    // When/Then - Test without required headers
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldCreateProduct() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    CreateProductRequest request = new CreateProductRequest()
            .name("Test Product")
            .brand("Test Brand")
            .categoryId(categoryId)
            .colorHex("#FF5733");

    // Create domain objects
    EntityId productId = new EntityId(UUID.randomUUID());
    EntityId categoryEntityId = new EntityId(categoryId);
    Category category = new Category(categoryEntityId, "Test Category");

    Product createdProduct = Product.reconstruct(
            productId,
            "Test Product",
            "Test Brand",
            category,
            "#FF5733",
            new ArrayList<>(),
            new HashSet<>()
    );

    ProductResponse expectedResponse = new ProductResponse()
            .id(productId.getValue())
            .name("Test Product")
            .brand("Test Brand")
            .colorHex("#FF5733")
            .averageRating(0.0f);

    // Configure mocks
    when(tokenService.extractUserId(anyString())).thenReturn(userId);
    when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(createdProduct);
    when(productResponseMapper.toResponse(createdProduct)).thenReturn(expectedResponse);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT)
                    .header("Authorization", "Bearer 4/P7q7W91")
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(productId.getValue().toString()))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.brand").value("Test Brand"))
            .andExpect(jsonPath("$.colorHex").value("#FF5733"))
            .andExpect(jsonPath("$.averageRating").value(0.0));

    // Verify that the mocks were called
    verify(tokenService).extractUserId(anyString());
    verify(createProductUseCase).execute(any(CreateProductCommand.class));
    verify(productResponseMapper).toResponse(createdProduct);
  }

  @Test
  void shouldReturnInternalServerErrorWhenCreateProductUseCaseFails() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    CreateProductRequest request = new CreateProductRequest()
            .name("Test Product")
            .brand("Test Brand")
            .categoryId(categoryId)
            .colorHex("#FF5733");

    // Configure mocks
    when(tokenService.extractUserId(anyString())).thenReturn(userId);
    when(createProductUseCase.execute(any(CreateProductCommand.class)))
            .thenThrow(new RuntimeException("Database connection error"));

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Request-ID", requestId.toString())
                    .header("X-Flow-ID", flowId.toString())
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("User-Agent", USER_AGENT)
                    .header("Authorization", "Bearer 4/P7q7W91")
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isInternalServerError());

    // Verify that the mock was called
    verify(tokenService).extractUserId(anyString());
    verify(createProductUseCase).execute(any(CreateProductCommand.class));
    verifyNoMoreInteractions(productResponseMapper);
  }


}