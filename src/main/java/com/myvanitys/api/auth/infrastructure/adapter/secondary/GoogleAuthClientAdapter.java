package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.dto.GoogleTokenResponse;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.dto.GoogleUserInfoResponse;
import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthClientAdapter implements GoogleAuthClient {

  private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

  private static final String USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

  private final WebClient webClient;

  private final GoogleClientProperties googleClientProperties;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<GoogleUserInfo> exchangeCodeForUserInfo(String authorizationCode, String redirectUri) {
    log.debug("Starting Google authentication process with authorization code");
    String effectiveRedirectUri = googleClientProperties.getRedirectUri();

    // Log the redirect URI being used for debugging
    log.debug("Using redirect URI: {}", redirectUri);
    log.debug("Configured redirect URI: {}", effectiveRedirectUri);

    return exchangeCodeForToken(authorizationCode, effectiveRedirectUri)
        .flatMap(tokenResponse -> fetchUserInfo(tokenResponse.getAccessToken()))
        .map(this::mapToDomainModel)
        .onErrorMap(WebClientResponseException.class, ex -> {
          log.error("Error during Google authentication: Status {}", ex.getStatusCode());
          return new GoogleAuthException("Failed to authenticate with Google: " + ex.getMessage(), ex);
        })
        .doOnTerminate(() -> log.debug("Google authentication process completed"));
  }

  protected Mono<GoogleTokenResponse> exchangeCodeForToken(String code, String redirectUri) {
    log.debug("Exchanging authorization code for access token");

    MultiValueMap<String, String> requestBody = getTokenRequestBody(code, redirectUri);

    // Log important parameters (without revealing sensitive information)
    log.debug("Token request parameters: client_id={}, redirect_uri={}, code_length={}",
        maskString(googleClientProperties.getClientId()),
        redirectUri,
        code != null ? code.length() : 0);

    return webClient.post()
        .uri(TOKEN_ENDPOINT)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(GoogleTokenResponse.class)
        .onErrorResume(WebClientResponseException.class, ex -> {
          log.error("Error retrieving token from Google. Status: {}", ex.getStatusCode());
          log.error("Response body: {}", ex.getResponseBodyAsString());

          try {
            JsonNode errorNode = objectMapper.readTree(ex.getResponseBodyAsString());
            String errorCode = errorNode.path("error").asText("unknown");
            String errorDescription = errorNode.path("error_description").asText("No description");

            log.error("Google OAuth error: {} - {}", errorCode, errorDescription);

            String detailedMessage = switch (errorCode) {
              case "invalid_grant" -> "The authorization code is invalid or has expired. Try to start the authorization process again.";
              case "redirect_uri_mismatch" -> String.format(
                  "The redirect URI (%s) does not match the one configured in Google Cloud Console. " +
                      "Verify that they are exactly the same.", redirectUri);
              case "invalid_client" -> "The client credentials (ID or secret) are incorrect. Verify the configuration.";
              case "invalid_request" -> "The request is invalid. Parameters are missing or incorrectly formatted: " + errorDescription;
              default -> "Error exchanging code for token: " + errorDescription;
            };

            return Mono.error(new GoogleAuthException(detailedMessage, ex));

          } catch (Exception e) {
            log.error("Error parsing Google's error response", e);
            return Mono.error(
                new GoogleAuthException("Error processing Google error response: " + ex.getResponseBodyAsString(), ex));
          }
        });
  }

  private MultiValueMap<String, String> getTokenRequestBody(String code, String redirectUri) {
    MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
    tokenRequestParams.add("code", code);
    tokenRequestParams.add("client_id", googleClientProperties.getClientId());
    tokenRequestParams.add("client_secret", googleClientProperties.getClientSecret());
    tokenRequestParams.add("redirect_uri", redirectUri);
    tokenRequestParams.add("grant_type", "authorization_code");
    return tokenRequestParams;
  }

  private Mono<GoogleUserInfoResponse> fetchUserInfo(String accessToken) {
    log.debug("Fetching user information with access token");

    return webClient.get()
        .uri(USER_INFO_ENDPOINT)
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .bodyToMono(GoogleUserInfoResponse.class)
        .onErrorMap(WebClientResponseException.class, ex -> {
          log.error("Error fetching user info from Google. Status: {}, Body: {}",
              ex.getStatusCode(), ex.getResponseBodyAsString());
          return new GoogleAuthException("Failed to fetch user info from Google", ex);
        });
  }

  private GoogleUserInfo mapToDomainModel(GoogleUserInfoResponse response) {
    if (response.getSub() == null || response.getEmail() == null || !isValidEmail(response.getEmail())) {
      log.warn("Google user info is missing required fields: id={}, email={}",
          response.getSub(), response.getEmail());
      throw new GoogleAuthException("Google user information is incomplete or invalid");
    }

    return new GoogleUserInfo(
        response.getSub(),
        response.getEmail(),
        response.getName() != null ? response.getName() : "",
        response.getPicture()
    );
  }

  // Additional method to validate the e-mail
  private boolean isValidEmail(String email) {
    return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
  }

  // Helper method to mask sensitive strings
  private String maskString(String input) {
    if (input == null) {
      return "null";
    }
    if (input.length() <= 8) {
      return "***";
    }
    return input.substring(0, 4) + "..." + input.substring(input.length() - 4);
  }
}

