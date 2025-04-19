package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;

  private final ProductMapper productMapper;

  @Override
  public Product save(Product product) {
    ProductEntity entity = productMapper.toEntity(product);
    ProductEntity savedEntity = jpaProductRepository.save(entity);
    return productMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Product> findById(EntityId productId) {
    UUID uuid = productId.getValue();
    return jpaProductRepository.findById(uuid)
        .map(productMapper::toDomain);
  }

  @Override
  public Optional<Product> findByName(String productName) {
    return jpaProductRepository.findByName(productName)
        .map(productMapper::toDomain);
  }

  @Override
  public List<Product> findByCategoryName(String categoryName) {
    return jpaProductRepository.findByCategoryName(categoryName).stream()
        .map(productMapper::toDomain)
        .toList();
  }

  @Override
  public void deleteById(EntityId productId) {
    UUID uuid = productId.getValue();
    jpaProductRepository.deleteById(uuid);
  }

  @Override
  public List<Product> findByUserId(UUID userId) {
    List<ProductEntity> productEntities = jpaProductRepository.findByUserId(userId);
    return productEntities.stream()
        .map(productMapper::toDomain)
        .toList();
  }

}
