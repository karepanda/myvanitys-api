package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.model.v1.CreateProductRequest;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;
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

  // Constants for required headers
  private static final String ACCEPT_LANGUAGE = "en-US";

  private static final String USER_AGENT = "Mozilla/5.0 (Test)";

  @Test
  void shouldFindProductsByUserId() throws Exception {
    // Given
    UUID requestId = UUID.randomUUID();
    UUID flowId = UUID.randomUUID();
    UUID userId = UUID.randomUUID(); // Make sure this is properly initialized
    String acceptLanguage = "en-US";
    String userAgent = "Mozilla/5.0 (Test)";

    // Print the userId to debug
    System.out.println("Test userId: " + userId);

    // Create domain model objects - Ensure entityId is properly constructed
    EntityId entityId = new EntityId(userId);
    System.out.println("EntityId value: " + entityId.getValue());

    Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");
    Category category2 = new Category(new EntityId(UUID.randomUUID()), "Test Category2");
    List<Product> domainProducts = Arrays.asList(
        new Product(new EntityId(UUID.randomUUID()), "Product 1", "Brand 1", category, "#FF0000"),
        new Product(new EntityId(UUID.randomUUID()), "Product 2", "Brand 2", category2, "#00FF00")
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
    // Instead of creating the query object here, let's just mock the behavior
    when(findProductUserUseCase.query(any(FindProductUserQuery.class))).thenReturn(domainProducts);
    when(productResponseMapper.toResponseList(domainProducts)).thenReturn(responseProducts);

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", acceptLanguage)
            .header("User-Agent", userAgent))
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

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .header("User-Agent", USER_AGENT))
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

    // When/Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{userId}/products", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", requestId.toString())
            .header("X-Flow-ID", flowId.toString())
            .header("Accept-Language", ACCEPT_LANGUAGE)
            .header("User-Agent", USER_AGENT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }
}