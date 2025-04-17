// infrastructure/adapter/secondary/dto/GoogleTokenResponse.java

package com.myvanitys.api.auth.infrastructure.adapter.secondary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoogleTokenResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("expires_in")
  private Integer expiresIn;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("id_token")
  private String idToken;

}