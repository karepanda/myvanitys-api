package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.AddProductToMyVanityCommand;
import com.myvanitys.api.product.domain.model.Product;


public interface AddProductToMyVanityUseCase {

    /**
     * Adds a product to the user's vanity list.
     *
     * @param command the command containing productId and userId
     * @return the added product
     * @throws IllegalArgumentException if the product is already associated with the user
     * @throws RuntimeException if the product does not exist
     */
    Product execute(AddProductToMyVanityCommand command);
}
