package com.myvanitys.api.auth.application.port.primary.command;

import java.time.Instant;
import java.util.Objects;

import lombok.NonNull;

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

}