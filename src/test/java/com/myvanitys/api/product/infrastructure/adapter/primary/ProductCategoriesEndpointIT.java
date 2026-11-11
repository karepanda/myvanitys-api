package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.security.TokenService;
import com.myvanitys.api.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ProductCategoriesEndpointIT extends AbstractIntegrationTest {

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    TokenService tokenServiceTest() {
      return mock(TokenService.class);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TokenService tokenService;

  @BeforeEach
  void authenticateRequest() {
    when(tokenService.extractUserId(anyString())).thenReturn(UUID.randomUUID());
  }

  @Test
  void getProductCategoriesReturnsEveryContractCodeFromTheSeededDatabase() throws Exception {
    mockMvc.perform(get("/api/v1/products/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", UUID.randomUUID())
            .header("X-Flow-ID", UUID.randomUUID())
            .header("Accept-Language", "en-US")
            .header("User-Agent", "ProductCategoriesEndpointIT")
            .header("Authorization", "Bearer integration-test-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(8)))
        .andExpect(jsonPath("$[*].code", containsInAnyOrder(
            "FACE",
            "EYES",
            "LASHES",
            "BROWS",
            "LIPS",
            "CREAM",
            "SERUM",
            "TONER")));
  }
}
