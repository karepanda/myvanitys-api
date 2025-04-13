package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import com.myvanitys.api.auth.domain.exception.GoogleAuthException;
import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import com.myvanitys.api.auth.domain.port.secondary.GoogleAuthClient;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.dto.GoogleTokenResponse;
import com.myvanitys.api.auth.infrastructure.adapter.secondary.dto.GoogleUserInfoResponse;
import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

  @Override
  public Mono<GoogleUserInfo> exchangeCodeForUserInfo(String authorizationCode, String redirectUri) {
    log.debug("Starting Google authentication process with authorization code");

    return exchangeCodeForToken(authorizationCode, redirectUri)
        .flatMap(tokenResponse -> fetchUserInfo(tokenResponse.getAccessToken()))
        .map(this::mapToDomainModel)
        .onErrorMap(WebClientResponseException.class, ex -> {
          log.error("Error during Google authentication", ex);
          return new GoogleAuthException("Failed to authenticate with Google: " + ex.getMessage(), ex);
        })
        .doOnTerminate(() -> log.debug("Google authentication process completed"));
  }

  public HttpEntity<MultiValueMap<String, String>> getHttpEntity(String authorizationCode, String redirectUri, HttpHeaders headers) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("code", authorizationCode);
    map.add("redirect_uri", redirectUri);
    map.add("client_id", googleClientProperties.getId());
    map.add("client_secret", googleClientProperties.getSecret());
    map.add("grant_type", "authorization_code");

    return new HttpEntity<>(map, headers);
  }

  protected Mono<GoogleTokenResponse> exchangeCodeForToken(String code, String redirectUri) {
    log.debug("Exchanging authorization code for access token");

    MultiValueMap<String, String> requestBody = getTokenRequestBody(code, redirectUri);

    return webClient.post()
        .uri(TOKEN_ENDPOINT)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(GoogleTokenResponse.class)
        .onErrorMap(WebClientResponseException.class, ex -> {
          log.error("Error retrieving token from Google", ex);
          return new GoogleAuthException("Failed to exchange authorization code for token", ex);
        });
  }

  private MultiValueMap<String, String> getTokenRequestBody(String code, String redirectUri) {
    MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
    tokenRequestParams.add("code", code);
    tokenRequestParams.add("client_id", googleClientProperties.getId());
    tokenRequestParams.add("client_secret", googleClientProperties.getSecret());
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
          log.error("Error fetching user info from Google", ex);
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
}

