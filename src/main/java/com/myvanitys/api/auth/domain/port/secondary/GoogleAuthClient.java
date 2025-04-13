package com.myvanitys.api.auth.domain.port.secondary;

import com.myvanitys.api.auth.domain.model.GoogleUserInfo;

/**
 * Port for communicating with Google's OAuth service to authenticate users.
 */
public interface GoogleAuthClient {

  /**
   * Exchanges a Google authorization code for user information.
   *
   * @param authorizationCode The authorization code returned by Google after successful user login. This code must be the exact code
   *     returned by Google and should be used only once.
   * @param redirectUri The redirect URI that was used during the initial authorization request. Must match exactly one of the URIs
   *     configured in the Google Cloud Console.
   * @return User information retrieved from Google.
   * @throws com.myvanitys.api.auth.domain.exception.GoogleAuthException if authentication fails.
   */
  GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode, String redirectUri);
}