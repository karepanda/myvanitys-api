package com.myvanitys.api.product.infrastructure.adapter.output;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.application.port.output.ProductRepositoryPort;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductJpaAdapter implements ProductRepositoryPort {

  private final ProductRepository productRepository;

  private final ProductMapper productMapper;

  public ProductJpaAdapter(ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  @Override
  public Optional<Product> findById(UUID productId) {
    return productRepository
        .findById(productId)
        .map(productMapper::toDomain);
  }

  @Override
  public Optional<Product> findByName(String productName) {
    return productRepository
        .findByName(productName)
        .map(productMapper::toDomain);
  }

  @Override
  public Optional<Product> findByCategoryName(String categoryName) {
    // Si productRepository.findByCategoryName devuelve una lista, podemos tomar el primero (si existe)
    return productRepository
        .findByCategoryName(categoryName)
        .stream()
        .findFirst()
        .map(productMapper::toDomain);
  }

  @Override
  @Transactional
  public Product save(Product product) {
    ProductEntity productEntity = productMapper.toEntity(product);
    ProductEntity savedEntity = productRepository.save(productEntity);
    return productMapper.toDomain(savedEntity);
  }
}