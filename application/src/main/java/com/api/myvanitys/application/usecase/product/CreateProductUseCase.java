package com.api.myvanitys.application.usecase.product;

import com.api.myvanitys.application.command.CreateProductCommand;
import com.api.myvanitys.domain.model.Product;
import com.api.myvanitys.domain.valueobject.EntityId;
import org.springframework.stereotype.Service;

@Service
public class CreateProductUseCase {

  public Product execute(CreateProductCommand command) {
    EntityId id = new EntityId();
    Product product = new Product(id, command.name(), command.description());

    // create Product
    return product;

  }

}
