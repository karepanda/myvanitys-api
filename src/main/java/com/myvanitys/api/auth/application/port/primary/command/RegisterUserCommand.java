package com.myvanitys.api.auth.application.port.primary.command;

import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

public record RegisterUserCommand(
    @NonNull String provider,
    @NonNull String code,
    String redirectUri,
    Instant registrationDate
) {

  public RegisterUserCommand {
    Objects.requireNonNull(provider, "provider is marked non-null but is null");
    Objects.requireNonNull(code, "code is marked non-null but is null");

  }

    public static RegisterUserCommand of(String provider, String code, String redirectUri) {
        return new RegisterUserCommand(provider, code, redirectUri, null);
    }

    public static RegisterUserCommand of(String provider, String code) {
        return new RegisterUserCommand(provider, code, null, null);
    }

}