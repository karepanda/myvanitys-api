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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class GoogleAuthClientAdapterIT {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  private GoogleAuthClientAdapter googleAuthClientAdapter;

  @BeforeEach
  void setUp() {
    // Configurar las propiedades
    GoogleClientProperties googleClientProperties = new GoogleClientProperties();
    googleClientProperties.setId("mock-client-id");
    googleClientProperties.setSecret("mock-client-secret");

    // Configurar WebClient con un Exchange Filter Function para redirigir las solicitudes
    WebClient webClient = WebClient.builder()
        .filter((request, next) -> {
          ClientRequest modifiedRequest = ClientRequest.from(request)
              .url(redirectUrl(request.url()))
              .build();
          return next.exchange(modifiedRequest);
        })
        .build();

    // Crear el adaptador
    googleAuthClientAdapter = new GoogleAuthClientAdapter(webClient, googleClientProperties);

    // Configurar los stubs
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
    // Redireccionar las URLs de Google a WireMock
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

    // Stubs para los endpoints reales que serán redirigidos
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

    // Verificar que las solicitudes fueron recibidas por WireMock
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_TokenEndpointError() {
    // Arrange
    String authorizationCode = "invalid-code";
    String redirectUri = "https://localhost/callback";

    // Sobrescribir el stub existente para el endpoint de token
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
                throwable.getMessage().contains("Failed to exchange authorization code for token"))
        .verify();

    // Verificar que se realizó la solicitud al endpoint de token
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
  }

  @Test
  void testExchangeCodeForUserInfo_UserInfoEndpointError() {
    // Arrange
    String authorizationCode = "valid-code-invalid-token";
    String redirectUri = "https://localhost/callback";

    // Sobrescribir el stub existente para el endpoint de userinfo
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

    // Verificar que se realizaron las solicitudes a ambos endpoints
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_InvalidUserInfo() {
    // Arrange
    String authorizationCode = "valid-code-invalid-user";
    String redirectUri = "https://localhost/callback";

    // Sobrescribir el stub existente para el endpoint de userinfo con datos incompletos
    wireMockServer.stubFor(post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"access_token\":\"mock-access-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));

    wireMockServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"sub\":\"12345\",\"name\":\"Test User\"}")));  // Sin email

    // Act & Assert
    Mono<GoogleUserInfo> result = googleAuthClientAdapter.exchangeCodeForUserInfo(authorizationCode, redirectUri);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof GoogleAuthException &&
                throwable.getMessage().contains("incomplete or invalid"))
        .verify();

    // Verificar que se realizaron las solicitudes a ambos endpoints
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_WithInvalidEmail() {
    // Arrange
    String authorizationCode = "valid-code-invalid-email";
    String redirectUri = "https://localhost/callback";

    // Sobrescribir el stub para responder con un email inválido
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

    // Verificar que se realizaron las solicitudes
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }

  @Test
  void testExchangeCodeForUserInfo_WithMissingName() {
    // Arrange
    String authorizationCode = "valid-code-missing-name";
    String redirectUri = "https://localhost/callback";

    // Sobrescribir el stub para responder sin name pero con los campos requeridos
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
          assertThat(googleUserInfo.name()).isEmpty(); // Nombre vacío
          assertThat(googleUserInfo.pictureUrl()).isEqualTo("https://test.com/pic.jpg");
        })
        .verifyComplete();

    // Verificar las solicitudes
    wireMockServer.verify(postRequestedFor(urlEqualTo("/token")));
    wireMockServer.verify(getRequestedFor(urlEqualTo("/userinfo")));
  }


}