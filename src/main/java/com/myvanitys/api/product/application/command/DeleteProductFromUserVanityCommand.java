package com.myvanitys.api.product.application.command;

import com.myvanitys.api.product.domain.valueobject.EntityId;

public record DeleteProductFromUserVanityCommand(
        EntityId productId,
        EntityId userId
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
