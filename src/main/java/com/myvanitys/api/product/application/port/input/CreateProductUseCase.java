package com.myvanitys.api.product.application.port.input;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateProductUseCase {

  ProductRepository productRepository;

  private final ProductMapper productMapper;

  public CreateProductUseCase(ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  public void execute(CreateProductCommand command) {

    final var product = new Product(new EntityId(),
        command.name(),
        command.brand(),
        command.categoryID(),
        command.colorHex());

    productRepository.save(productMapper.toEntity(product));

  }

}
