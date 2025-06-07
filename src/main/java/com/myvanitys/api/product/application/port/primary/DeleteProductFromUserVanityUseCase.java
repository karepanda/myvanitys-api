package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;

public interface DeleteProductFromUserVanityUseCase {
    void execute(DeleteProductFromUserVanityCommand command);
}
