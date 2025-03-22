package com.myvanitys.api.product.application.port.input;

import java.util.UUID;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.springframework.stereotype.Service;

@Service
public class CreateProductUseCase {

  public UUID execute(CreateProductCommand command) {

    final var product = new Product(new EntityId(),
        command.name(),
        command.brand(),
        command.categoryID(),
        command.colorHex());

    // create Product
    return product.getId().getValue();
  }

}
