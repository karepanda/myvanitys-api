package com.myvanitys.api.product.application.port.primary;

import java.util.Optional;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public interface FindProductUseCase {

  Optional<Product> execute(EntityId productId);

}
