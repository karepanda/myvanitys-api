package com.myvanitys.api.product.application.service;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductUseCase;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.service.ProductService;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Manages the product lifecycle by coordinating between domain entities and infrastructure. Implements use cases for product-related
 * operations.
 */
@Service
@AllArgsConstructor
public class ProductManager implements ProductService, CreateProductUseCase, FindProductUseCase {

  private final ProductRepository productRepository;

  private final ReviewRepository reviewRepository;

  private final ProductUserRepository productUserRepository;

  private final CategoryRepository categoryRepository;

  @Override
  public Product createProduct(String name, String brand, Category category, String colorHex, EntityId userId) {
    return null;
  }

  @Override
  public Review addReviewToProduct(EntityId productId, String comment, int rating, EntityId userId) {
    return null;
  }

  @Override
  public Product updateProduct(EntityId productId, String name, String brand, Category category, String colorHex) {
    return null;
  }

  @Override
  public void deleteProduct(EntityId productId) {

  }

  @Override
  public Optional<Product> getProductById(EntityId productId) {
    // Here you'd implement the actual functionality to find a product by ID
    return productRepository.findById(productId);
  }

  @Override
  public List<Product> getProductsByUserId(EntityId userId) {
    return List.of();
  }

  @Override
  public Review updateReview(EntityId reviewId, String comment, int rating) {
    return null;
  }

  @Override
  public void deleteReview(EntityId reviewId) {

  }

  @Override
  public List<Review> getReviewsByProductId(EntityId productId) {
    return List.of();
  }

  @Override
  public boolean isProductOwner(EntityId productId, EntityId userId) {
    return false;
  }

  @Override
  public boolean isReviewOwner(EntityId reviewId, EntityId userId) {
    return false;
  }

  @Override
  public Product execute(CreateProductCommand command) {
    return null;
  }

  @Override
  public Optional<Product> execute(EntityId productId) {
    return getProductById(productId);
  }
}
