package com.myvanitys.api.auth.domain.model;

public record UserSession(String token, User user) {

  public UserSession {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Token cannot be null or blank");
    }
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
  }

  public boolean isActive() {
    return token != null && !token.isBlank();
  }

  public static UserSession create(String token, User user) {
    return new UserSession(token, user);
  }

  public String email() {
    return user.getEmail();
  }

  public String name() {
    return user.getName();
  }

  public String googleId() {
    return user.getAuthorizationId();
  }
}
