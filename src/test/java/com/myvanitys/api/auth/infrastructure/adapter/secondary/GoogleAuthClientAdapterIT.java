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
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GoogleAuthClientAdapterIT {

  private static final String TOKEN_PATH = "/token";
  private static final String USER_INFO_PATH = "/userinfo";
  private static final String CONFIGURED_REDIRECT_URI = "https://localhost/callback";
  private static final String AUTHORIZATION_CODE = "test-authorization-code";

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  private GoogleAuthClientAdapter googleAuthClientAdapter;

  @BeforeEach
  void setUp() {
    // Deterministic isolation: drop any stub left by a previous test before re-adding defaults.
    wireMockServer.resetAll();

    // Set up properties
    GoogleClientProperties googleClientProperties = new GoogleClientProperties();
    googleClientProperties.setClientId("mock-client-id");
    googleClientProperties.setClientSecret("mock-client-secret");
    googleClientProperties.setRedirectUri(CONFIGURED_REDIRECT_URI);

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
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
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
      return URI.create(wireMockServer.baseUrl() + TOKEN_PATH);
    } else if (url.equals("https://www.googleapis.com/oauth2/v3/userinfo")) {
      return URI.create(wireMockServer.baseUrl() + USER_INFO_PATH);
    }
    return originalUrl;
  }

  private void stubTokenError(String errorCode, String errorDescription) {
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"" + errorCode + "\",\"error_description\":\"" + errorDescription + "\"}")));
  }

  @Test
  void testExchangeCodeForUserInfo() {
    // Arrange
    String redirectUri = CONFIGURED_REDIRECT_URI;

    // Stubs for real endpoints that will be redirected
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                "{\"sub\":\"12345\",\"email\":\"testuser@example.com\",\"name\":\"Test User\",\"picture\":\"https://test.com/pic.jpg\"}")));

    // Act
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(AUTHORIZATION_CODE, redirectUri);

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
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  @Test
  void testExchangeCodeForUserInfo_TokenEndpointError() {
    // Arrange
    stubTokenError("invalid_grant", "Invalid authorization code");

    // Act & Assert
    Mono<GoogleUserInfo> result =
        googleAuthClientAdapter.exchangeCodeForUserInfo("invalid-code", CONFIGURED_REDIRECT_URI);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage()
                    .contains("The authorization code is invalid or has expired. Try to start the authorization process again"))
        .verify();

    // Verify request to token endpoint and that user info was never requested
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(0, getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  static Stream<Arguments> tokenErrorScenarios() {
    return Stream.of(
        Arguments.of(
            "redirect_uri_mismatch",
            "Redirect URI does not match",
            "The redirect URI (" + CONFIGURED_REDIRECT_URI + ") does not match the one configured in Google Cloud Console. "
                + "Verify that they are exactly the same."),
        Arguments.of(
            "invalid_client",
            "Invalid client credentials",
            "The client credentials (ID or secret) are incorrect. Verify the configuration."),
        Arguments.of(
            "invalid_request",
            "client_id is required",
            "The request is invalid. Parameters are missing or incorrectly formatted: client_id is required"),
        Arguments.of(
            "temporarily_unavailable",
            "Google OAuth is temporarily unavailable",
            "Error exchanging code for token: Google OAuth is temporarily unavailable"));
  }

  @ParameterizedTest(name = "token error [{0}]")
  @MethodSource("tokenErrorScenarios")
  void testExchangeCodeForUserInfo_TokenErrorSwitchBranches(
      String errorCode, String errorDescription, String expectedMessage) {
    // Arrange
    stubTokenError(errorCode, errorDescription);

    // Act
    Mono<GoogleUserInfo> result =
        googleAuthClientAdapter.exchangeCodeForUserInfo(AUTHORIZATION_CODE, CONFIGURED_REDIRECT_URI);

    // Assert
    StepVerifier.create(result)
        .expectErrorSatisfies(throwable -> {
          assertThat(throwable).isInstanceOf(GoogleAuthException.class);
          assertThat(throwable.getMessage()).contains(expectedMessage);
          assertThat(throwable).hasCauseInstanceOf(WebClientResponseException.class);
        })
        .verify();

    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(0, getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  @Test
  void testExchangeCodeForUserInfo_MalformedErrorResponse() {
    // Arrange: a 400 whose body cannot be parsed as JSON exercises the catch(Exception) branch.
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("not-json")));

    // Act
    Mono<GoogleUserInfo> result =
        googleAuthClientAdapter.exchangeCodeForUserInfo(AUTHORIZATION_CODE, CONFIGURED_REDIRECT_URI);

    // Assert
    StepVerifier.create(result)
        .expectErrorSatisfies(throwable -> {
          assertThat(throwable).isInstanceOf(GoogleAuthException.class);
          assertThat(throwable.getMessage())
              .contains("Error processing Google error response")
              .contains("not-json");
          assertThat(throwable).hasCauseInstanceOf(WebClientResponseException.class);
        })
        .verify();

    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(0, getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  static Stream<Arguments> maskStringScenarios() {
    return Stream.of(
        Arguments.of(null, "null"),
        Arguments.of("", "***"),
        Arguments.of("12345678", "***"),
        Arguments.of("123456789", "1234...6789"),
        Arguments.of("mock-client-id", "mock...t-id"));
  }

  // maskString is private and only feeds logging, so its output is not observable through the
  // public contract. ReflectionTestUtils invokes this pure helper without starting a Spring context.
  @ParameterizedTest(name = "maskString -> {1}")
  @MethodSource("maskStringScenarios")
  void maskString_masksSensitiveValues(String input, String expected) {
    Object masked = ReflectionTestUtils.invokeMethod(googleAuthClientAdapter, "maskString", (String) input);

    assertThat(masked).isEqualTo(expected);
  }

  @Test
  void testExchangeCodeForUserInfo_UserInfoEndpointError() {
    // Arrange
    String authorizationCode = "valid-code-invalid-token";
    String redirectUri = CONFIGURED_REDIRECT_URI;

    // Overwrite existing stub for userinfo endpoint
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"invalid-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
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
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  @Test
  void testExchangeCodeForUserInfo_InvalidUserInfo() {
    // Arrange
    String authorizationCode = "valid-code-invalid-user";
    String redirectUri = CONFIGURED_REDIRECT_URI;

    // Overwrite stub for userinfo endpoint with incomplete data
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
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
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  @Test
  void testExchangeCodeForUserInfo_WithInvalidEmail() {
    // Arrange
    String authorizationCode = "valid-code-invalid-email";
    String redirectUri = CONFIGURED_REDIRECT_URI;

    // Overwrite stub to return an invalid email
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
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
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

  @Test
  void testExchangeCodeForUserInfo_WithMissingName() {
    // Arrange
    String authorizationCode = "valid-code-missing-name";
    String redirectUri = CONFIGURED_REDIRECT_URI;

    // Overwrite stub to respond without a name but with required fields
    wireMockServer.stubFor(post(urlEqualTo(TOKEN_PATH))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo(USER_INFO_PATH))
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
    wireMockServer.verify(postRequestedFor(urlEqualTo(TOKEN_PATH)));
    wireMockServer.verify(getRequestedFor(urlEqualTo(USER_INFO_PATH)));
  }

}
