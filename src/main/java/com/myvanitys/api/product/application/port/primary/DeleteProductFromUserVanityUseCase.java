package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.domain.model.Product;

public interface DeleteProductFromUserVanityUseCase {
    Product execute(DeleteProductFromUserVanityCommand command);
}
