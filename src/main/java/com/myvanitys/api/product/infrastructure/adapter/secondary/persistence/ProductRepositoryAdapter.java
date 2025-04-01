package com.myvanitys.api.product.infrastructure.adapter.secondary.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;

  private final JpaProductUserRepository jpaProductUserRepository;

  private final ProductMapper productMapper;

  public ProductRepositoryAdapter(
      JpaProductRepository jpaProductRepository,
      JpaProductUserRepository jpaProductUserRepository,
      ProductMapper productMapper) {
    this.jpaProductRepository = jpaProductRepository;
    this.jpaProductUserRepository = jpaProductUserRepository;
    this.productMapper = productMapper;
  }

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
    return Optional.empty();
  }

  @Override
  public List<Product> findByCategoryName(String categoryName) {
    return List.of();
  }

  @Override
  public void deleteById(EntityId productId) {
    UUID uuid = productId.getValue();
    jpaProductRepository.deleteById(uuid);
  }

  @Override
  public List<Product> findByUserId(EntityId userId) {
    UUID uuid = userId.getValue();
    return jpaProductUserRepository.findByUserId(uuid).stream()
        .map(ProductUserEntity::getProductId)
        .map(jpaProductRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(productMapper::toDomain)
        .toList();
  }

}
