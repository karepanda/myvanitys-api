package com.myvanitys.api.product.application.port.input;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.CategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

  private final ProductRepository productRepository;

  private final ProductMapper productMapper;

  private final CategoryRepository categoryRepository; // Añadido para obtener la categoría

  public CreateProductUseCase(
      ProductRepository productRepository,
      ProductMapper productMapper,
      CategoryRepository categoryRepository) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public Product execute(CreateProductCommand command) {
    // Validate the command
    command.validate();

    // Get the category from the ID
    Category category = categoryRepository.findById(command.categoryID())
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

    // Persist the product and its relationships
    var savedProductEntity = productRepository.save(productMapper.toEntity(product));

    // The mapper should handle converting user relationships as well
    return productMapper.toDomain(savedProductEntity);
  }
}