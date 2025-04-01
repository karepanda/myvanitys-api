package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.model.Product;

public interface CreateProductUseCase {

  Product execute(CreateProductCommand command);
}
