package com.myvanitys.api.product.application.command;

import java.util.UUID;

public record DeleteProductFromUserVanityCommand(
        UUID productId,
        UUID userId
) {
    public DeleteProductFromUserVanityCommand {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

}
