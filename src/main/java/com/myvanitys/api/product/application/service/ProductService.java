package com.myvanitys.api.product.application.service;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.input.CreateProductUseCase;
import com.myvanitys.api.product.application.port.output.CategoryRepositoryPort;
import com.myvanitys.api.product.application.port.output.ProductRepositoryPort;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements CreateProductUseCase {

  private final ProductRepositoryPort productRepository;

  private final CategoryRepositoryPort categoryRepository;

  public ProductService(
      ProductRepositoryPort productRepository,
      CategoryRepositoryPort categoryRepository) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
  }

  @Override
  @Transactional
  public Product execute(CreateProductCommand command) {
    // Validate the command
    command.validate();

    // Get the category from the ID
    Category category = categoryRepository.findById(command.categoryId())
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    // Create the product
    final var product = new Product(
        command.id(),
        command.name(),
        command.brand(),
        category,
        command.colorHex());

    // Add the product to the user's vanity
    product.addToUserVanity(command.userId(), command.reviewText());

    // Persist the product
    return productRepository.save(product);
  }
}