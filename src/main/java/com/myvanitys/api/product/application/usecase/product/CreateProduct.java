package com.myvanitys.api.product.application.usecase.product;



import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
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
