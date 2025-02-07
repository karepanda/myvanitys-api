package com.api.myvanitys.application.usecase.product;

import com.api.myvanitys.application.command.CreateProductCommand;
import com.api.myvanitys.domain.model.Product;
import com.api.myvanitys.domain.valueobject.EntityId;
import org.springframework.stereotype.Service;

@Service
public class CreateProduct {

  public Product execute(CreateProductCommand command) {
    EntityId id = new EntityId();
    Product product = new Product(id, command.name(), command.brand(), command.categoryID(), command.colorHex());

    // create Product
    return product;

  }

}
