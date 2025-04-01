package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, EntityIdMapper.class})
public abstract class ReviewMapper {

  @Autowired
  protected JpaProductRepository jpaProductRepository;

  @Autowired
  protected JpaProductUserRepository jpaProductUserRepository;

  @Autowired
  protected ProductMapper productMapper;

  @Mapping(target = "id", source = "reviewId")
  @Mapping(target = "userId", source = "productUserEntity.userId")
  @Mapping(target = "product", source = "productUserEntity.productId", qualifiedByName = "productIdToProduct")
  public abstract Review toDomain(ReviewEntity reviewEntity);

  @Mapping(target = "reviewId", source = "id")
  @Mapping(target = "productUserEntity", source = ".", qualifiedByName = "findProductUserEntity")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract ReviewEntity toEntity(Review review);

  @Named("productIdToProduct")
  protected Product productIdToProduct(UUID productId) {
    if (productId == null) {
      return null;
    }

    // Buscar el producto usando el repositorio inyectado
    return jpaProductRepository.findById(productId)
        .map(productMapper::toDomain)
        .orElse(null);
  }

  @Named("findProductUserEntity")
  protected ProductUserEntity findProductUserEntity(Review review) {
    UUID userId = review.getUserId().getValue();
    UUID productId = review.getProduct().getId().getValue();

    // Usar el método existente en el repositorio inyectado
    return jpaProductUserRepository.findByProductIdAndUserId(productId, userId);
  }

  protected JpaProductRepository getProductRepository() {
    return jpaProductRepository;
  }

  protected JpaProductUserRepository getProductUserRepository() {
    return jpaProductUserRepository;
  }

  protected ProductMapper getProductMapper() {
    return productMapper;
  }
}