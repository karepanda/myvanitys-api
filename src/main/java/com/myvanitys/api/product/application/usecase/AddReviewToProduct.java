package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.application.port.primary.AddReviewToProductUseCase;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddReviewToProduct implements AddReviewToProductUseCase {

  private final ProductRepository productRepository;

  @Override
  @Transactional
  public Product execute(AddReviewToProductCommand command) {
    Product product = findProductOrThrow(command.productId());

    product.addReviewFromUser(command.userId(), command.reviewDetails());

    return productRepository.save(product);
  }

  /**
   * Helper method to find a product by ID or throw an exception
   *
   * @param productId The product ID
   * @return The found product
   * @throws ProductNotFoundException if the product is not found
   */
  private Product findProductOrThrow(EntityId productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found" + productId));
  }
}
