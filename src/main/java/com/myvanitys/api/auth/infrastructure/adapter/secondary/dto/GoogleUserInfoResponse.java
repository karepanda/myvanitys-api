// infrastructure/adapter/secondary/dto/GoogleUserInfoResponse.java

package com.myvanitys.api.auth.infrastructure.adapter.secondary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoogleUserInfoResponse {

  @JsonProperty("sub")
  private String sub;

  @JsonProperty("name")
  private String name;

  @JsonProperty("given_name")
  private String givenName;

  @JsonProperty("family_name")
  private String familyName;

  @JsonProperty("picture")
  private String picture;

  @JsonProperty("email")
  private String email;

  @JsonProperty("email_verified")
  private Boolean emailVerified;

  @JsonProperty("locale")
  private String locale;

}