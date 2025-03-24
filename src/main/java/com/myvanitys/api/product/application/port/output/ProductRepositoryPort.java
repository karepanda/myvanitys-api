package com.myvanitys.api.product.application.port.output;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.Product;

public interface ProductRepositoryPort {
  Product findById(UUID productId);
  Optional<Product> findByName(String productName);
  Optional<Product> findByCategoryName(String categoryName);
  Product save(Product product);

}
