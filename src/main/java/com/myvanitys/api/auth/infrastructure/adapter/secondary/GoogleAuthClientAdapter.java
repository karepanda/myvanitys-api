// infrastructure/adapter/secondary/GoogleAuthClientAdapter.java

package com.myvanitys.api.auth.infrastructure.adapter.secondary;

import java.util.Optional;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthClientAdapter implements GoogleAuthClient {

  private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

  private static final String USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

  private final RestTemplate restTemplate;

  private final GoogleClientProperties googleClientProperties;

  @Override
  public GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode, String redirectUri) {
    log.debug("Starting Google authentication process with authorization code");

    try {
      // Step 1: Get access token
      GoogleTokenResponse tokenResponse = exchangeCodeForToken(authorizationCode, redirectUri);

      // Step 2: Get user info with the token
      GoogleUserInfoResponse userInfoResponse = fetchUserInfo(tokenResponse.getAccessToken());

      // Step 3: Map to domain model
      return mapToDomainModel(userInfoResponse);
    } catch (RestClientException e) {
      log.error("Error communicating with Google API", e);
      throw new GoogleAuthException("Failed to authenticate with Google: " + e.getMessage(), e);
    }
  }

  private GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
    log.debug("Exchanging authorization code for access token");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    final HttpEntity<MultiValueMap<String, String>> request =
        getHttpEntity(code, redirectUri, headers);

    GoogleTokenResponse response = restTemplate.postForObject(
        TOKEN_ENDPOINT, request, GoogleTokenResponse.class);

    return Optional.ofNullable(response)
        .filter(r -> r.getAccessToken() != null)
        .orElseThrow(() -> {
          log.error("Failed to obtain access token from Google");
          return new GoogleAuthException("Access token not received from Google");
        });
  }

  private HttpEntity<MultiValueMap<String, String>> getHttpEntity(String code, String redirectUri, HttpHeaders headers) {
    MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
    tokenRequestParams.add("code", code);
    tokenRequestParams.add("client_id", googleClientProperties.getId());
    tokenRequestParams.add("client_secret", googleClientProperties.getSecret());
    tokenRequestParams.add("redirect_uri", redirectUri);
    tokenRequestParams.add("grant_type", "authorization_code");

    return new HttpEntity<>(tokenRequestParams, headers);
  }

  private GoogleUserInfoResponse fetchUserInfo(String accessToken) {
    log.debug("Fetching user information with access token");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> request = new HttpEntity<>(headers);

    ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
        USER_INFO_ENDPOINT,
        HttpMethod.GET,
        request,
        GoogleUserInfoResponse.class
    );

    return Optional.ofNullable(response.getBody())
        .orElseThrow(() -> {
          log.error("Received null response body from Google UserInfo endpoint");
          return new GoogleAuthException("Failed to retrieve user information from Google");
        });
  }

  private GoogleUserInfo mapToDomainModel(GoogleUserInfoResponse response) {
    // Validate required fields
    if (response.getSub() == null || response.getEmail() == null) {
      log.warn("Google user info is missing required fields: id={}, email={}",
          response.getSub(), response.getEmail());
      throw new GoogleAuthException("Google user information is incomplete");
    }

    log.debug("Successfully retrieved and mapped Google user information");

    return new GoogleUserInfo(
        response.getSub(),
        response.getEmail(),
        response.getName() != null ? response.getName() : "",
        response.getPicture()
    );
  }
}