package com.myvanitys.api.product.application.command;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public record AddProductToMyVanityCommand(@NotNull UUID productId, @NotNull UUID userId) {

    public AddProductToMyVanityCommand {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
    }

}
