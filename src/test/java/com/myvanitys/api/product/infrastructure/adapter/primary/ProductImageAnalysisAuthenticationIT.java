package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@SpringBootTest(properties = {
    "spring.docker.compose.enabled=false",
    "spring.datasource.url=jdbc:postgresql://localhost/unused",
    "spring.datasource.username=unused",
    "spring.datasource.password=unused",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED)
class ProductImageAnalysisAuthenticationIT {

  private static final String ENDPOINT = "/api/v1/products/image-analysis";
  private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVR4nGNgAAAAAgABSK+kcQAAAABJRU5ErkJggg==");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Key jwtSigningKey;

  @MockitoSpyBean
  private ProductController productController;

  private final UUID requestId = UUID.randomUUID();
  private final UUID flowId = UUID.randomUUID();
  private final MockMultipartFile image =
      new MockMultipartFile("image", "product.png", MediaType.IMAGE_PNG_VALUE, ONE_PIXEL_PNG);

  @Test
  void rejectsRequestWithoutToken() throws Exception {
    assertUnauthorized(mockMvc.perform(imageAnalysisRequest()));
  }

  @Test
  void rejectsRequestWithInvalidTokenSignature() throws Exception {
    String token = signedToken(Jwts.SIG.HS256.key().build(), Instant.now().plusSeconds(300));

    assertUnauthorized(mockMvc.perform(imageAnalysisRequest()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)));
  }

  @Test
  void rejectsRequestWithExpiredToken() throws Exception {
    String token = signedToken((SecretKey) jwtSigningKey, Instant.now().minusSeconds(300));

    assertUnauthorized(mockMvc.perform(imageAnalysisRequest()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)));
  }

  @Test
  void forwardsRequestWithValidTokenToImageAnalysisHandler() throws Exception {
    String token = signedToken((SecretKey) jwtSigningKey, Instant.now().plusSeconds(300));

    mockMvc.perform(imageAnalysisRequest().header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(handler().methodName("analyzeProductImage"));

    verify(productController).analyzeProductImage(requestId, flowId, image);
  }

  private MockMultipartHttpServletRequestBuilder imageAnalysisRequest() {
    return multipart(ENDPOINT)
        .file(image)
        .header("X-Request-ID", requestId)
        .header("X-Flow-ID", flowId);
  }

  private void assertUnauthorized(ResultActions response) throws Exception {
    response.andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("https://api.myvanitys.com/problems/unauthorized"))
        .andExpect(jsonPath("$.title").isString())
        .andExpect(jsonPath("$.title").value(not(blankOrNullString())))
        .andExpect(jsonPath("$.status").value(is(401)))
        .andExpect(jsonPath("$.detail").isString())
        .andExpect(jsonPath("$.instance").value(ENDPOINT));

    verifyNoInteractions(productController);
  }

  private String signedToken(SecretKey key, Instant expiresAt) {
    return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .expiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
  }
}
