package com.myvanitys.api.product.application.usecase;

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

import java.util.Optional;

@Service
@AllArgsConstructor
public class CreateProduct implements CreateProductUseCase {

  protected ProductRepository productRepository;

  protected CategoryRepository categoryRepository;

  protected ProductUserRepository productUserRepository;

  @Override
  @Transactional
  public Product execute(CreateProductCommand command) {
    Optional<Product> existingProduct = productRepository.findByName(command.name(), command.userId());

    if (existingProduct.isPresent()) {
      productUserRepository.saveProductUserRelationship(existingProduct.get().getId(), command.userId());
      return existingProduct.get();
    }

    Category category = categoryRepository.findById(command.categoryId())
        .orElseThrow(() -> ValidationException.withError("categoryId",
            "Category not found with ID: " + command.categoryId()));

    Product product = Product.create(command.name(), command.brand(), category, command.colorHex());

    Product savedProduct = productRepository.save(product);

    productUserRepository.saveProductUserRelationship(savedProduct.getId(), command.userId());

    return savedProduct;
  }
}
