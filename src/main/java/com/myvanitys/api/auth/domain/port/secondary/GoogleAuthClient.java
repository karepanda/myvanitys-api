package com.myvanitys.api.auth.domain.port.secondary;

import com.myvanitys.api.auth.domain.model.GoogleUserInfo;
import reactor.core.publisher.Mono;

/**
 * Port for communicating with Google's OAuth service to authenticate users.
 */
public interface GoogleAuthClient {

  /**
   * Exchanges a Google authorization code for user information.
   *
   * @param authorizationCode The authorization code returned by Google after a successful login. This code must be the exact code
   *     returned by Google and must be used only once.
   * @param redirectUri The redirect URI that was used during the initial authorization request. It must match exactly one of the URIs
   *     configured in the Google Cloud Console.
   * @return User information obtained from Google wrapped in a Mono (reactive).
   * @throws com.myvanitys.api.auth.domain.exception.GoogleAuthException if authentication fails.
   */
  Mono<GoogleUserInfo> exchangeCodeForUserInfo(String authorizationCode, String redirectUri);
}