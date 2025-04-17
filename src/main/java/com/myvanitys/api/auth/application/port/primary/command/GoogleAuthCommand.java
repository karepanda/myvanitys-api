package com.myvanitys.api.auth.application.port.primary.command;

public record GoogleAuthCommand(String code, String redirectUri) {

  public GoogleAuthCommand {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("Authorization code cannot be null or blank");
    }

  }

  public static GoogleAuthCommand of(String code, String redirectUri) {
    return new GoogleAuthCommand(code, redirectUri);
  }

  public static GoogleAuthCommand of(String code) {
    return new GoogleAuthCommand(code, null);
  }
}