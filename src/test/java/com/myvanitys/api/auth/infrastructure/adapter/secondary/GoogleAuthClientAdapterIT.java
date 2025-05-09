package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import com.myvanitys.api.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GoogleAuthClientAdapterIT extends AbstractIntegrationTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  private GoogleAuthClientAdapter googleAuthClientAdapter;

  @BeforeEach
  void setUp() {
    // Set up properties
    GoogleClientProperties googleClientProperties = new GoogleClientProperties();
    googleClientProperties.setClientId("mock-client-id");
    googleClientProperties.setClientSecret("mock-client-secret");
    googleClientProperties.setRedirectUri("https://localhost/callback");

    // Configure WebClient with an Exchange Filter Function to redirect requests
    WebClient webClient = WebClient.builder()
        .filter((request, next) -> {
          ClientRequest modifiedRequest = ClientRequest.from(request)
              .url(redirectUrl(request.url()))
              .build();
          return next.exchange(modifiedRequest);
        })
        .build();

    // Create the adapter
    googleAuthClientAdapter = new GoogleAuthClientAdapter(webClient, googleClientProperties);

    // Set up stubs
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "{\"sub\":\"12345\",\"email\":\"testuser@example.com\",\"name\":\"Test User\",\"picture\":\"https://test.com/pic.jpg\"}")));
  }

  private URI redirectUrl(URI originalUrl) {
    // Redirect Google URLs to WireMock
    String url = originalUrl.toString();
    if (url.equals("https://oauth2.googleapis.com/token")) {
      return URI.create(wireMockServer.baseUrl() + "/token");
    } else if (url.equals("https://www.googleapis.com/oauth2/v3/userinfo")) {
      return URI.create(wireMockServer.baseUrl() + "/userinfo");
    }
    return originalUrl;
  }

  @Test
  void testExchangeCodeForUserInfo() {
    // Arrange
    String authorizationCode = "test-authorization-code";
    String redirectUri = "https://localhost/callback";

    // Stubs for real endpoints that will be redirected
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "{\"sub\":\"12345\",\"email\":\"testuser@example.com\",\"name\":\"Test User\",\"picture\":\"https://test.com/pic.jpg\"}")));

    // Act
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    // Assert using StepVerifier
    StepVerifier.create(result)
        .assertNext(googleUserInfo -> {
          assertThat(googleUserInfo.id()).isEqualTo("12345");
          assertThat(googleUserInfo.email()).isEqualTo("testuser@example.com");
          assertThat(googleUserInfo.name()).isEqualTo("Test User");
          assertThat(googleUserInfo.pictureUrl()).isEqualTo("https://test.com/pic.jpg");
        })
        .verifyComplete();

    // Verify that the requests were received by WireMock
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_TokenEndpointError() {
    // Arrange
    String authorizationCode = "invalid-code";
    String redirectUri = "https://localhost/callback";

    // Overwrite existing stub for token endpoint
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid authorization code\"}")));

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage()
                    .contains("The authorization code is invalid or has expired. Try to start the authorization process again"))
        .verify();

    // Verify request to token endpoint
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
  }

  @Test
  void testExchangeCodeForUserInfo_UserInfoEndpointError() {
    // Arrange
    String authorizationCode = "valid-code-invalid-token";
    String redirectUri = "https://localhost/callback";

    // Overwrite existing stub for userinfo endpoint
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"invalid-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(401)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"invalid_token\"}")));

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage().contains("Failed to fetch user info from Google"))
        .verify();

    // Verify requests to both endpoints
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_InvalidUserInfo() {
    // Arrange
    String authorizationCode = "valid-code-invalid-user";
    String redirectUri = "https://localhost/callback";

    // Overwrite stub for userinfo endpoint with incomplete data
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"sub\":\"12345\",\"name\":\"Test User\"}")));  // Missing email

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage().contains("incomplete or invalid"))
        .verify();

    // Verify requests to both endpoints
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_WithInvalidEmail() {
    // Arrange
    String authorizationCode = "valid-code-invalid-email";
    String redirectUri = "https://localhost/callback";

    // Overwrite stub to return an invalid email
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"sub\":\"12345\",\"email\":\"not-an-email\",\"name\":\"Test User\",\"picture\":\"https://test.com/pic.jpg\"}")));

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage().contains("incomplete or invalid"))
        .verify();

    // Verify requests
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_WithMissingName() {
    // Arrange
    String authorizationCode = "valid-code-missing-name";
    String redirectUri = "https://localhost/callback";

    // Overwrite stub to respond without a name but with required fields
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"sub\":\"12345\",\"email\":\"testuser@example.com\",\"picture\":\"https://test.com/pic.jpg\"}")));

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .assertNext(googleUserInfo -> {
          assertThat(googleUserInfo.id()).isEqualTo("12345");
          assertThat(googleUserInfo.email()).isEqualTo("testuser@example.com");
          assertThat(googleUserInfo.name()).isEmpty(); // Empty name
          assertThat(googleUserInfo.pictureUrl()).isEqualTo("https://test.com/pic.jpg");
        })
        .verifyComplete();

    // Verify requests
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

}