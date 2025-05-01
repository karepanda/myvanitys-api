package com.myvanitys.api.product.application.usecase;

import java.util.Optional;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CreateProduct implements CreateProductUseCase {

  protected ProductRepository productRepository;

  protected CategoryRepository categoryRepository;

  protected ProductUserRepository productUserRepository;

  @Override
  @Transactional
  public Product execute(CreateProductCommand command) {
    // Verify if the product already exists by name
    Optional<Product> existingProduct = productRepository.findByName(command.name());

    if (existingProduct.isPresent()) {
      // If the product already exists, we only associate the user if he/she is not already associated.
      productUserRepository.saveProductUserRelationship(existingProduct.get().getId(), command.userId());
      return existingProduct.get();
    }

    // If the product does not exist, we create a new one.
    Category category = categoryRepository.findById(command.categoryId())
        .orElseThrow(() -> ValidationException.withError("categoryId",
            "Category not found with ID: " + command.categoryId()));

    // Create product
    Product product = Product.create(command.name(), command.brand(), category, command.colorHex());

    Product savedProduct = productRepository.save(product);

    // Associate the product with the user who creates it
    productUserRepository.saveProductUserRelationship(savedProduct.getId(), command.userId());

    return savedProduct;
  }
}
