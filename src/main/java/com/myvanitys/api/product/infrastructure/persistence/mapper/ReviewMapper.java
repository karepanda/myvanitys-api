package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ReviewMapper {

  @Autowired
  protected ProductRepository productRepository;

  @Autowired
  protected ProductMapper productMapper;

  public Review toDomain(ReviewEntity entity) {
    if (entity == null) {
      return null;
    }

    // Obtener el producto relacionado
    Product product = null;
    if (entity.getProductUserEntity() != null) {
      UUID productId = entity.getProductUserEntity().getProductId();
      product = productMapper.toDomain(
          productRepository.findById(productId)
              .orElseThrow(() -> new RuntimeException("Product not found"))
      );
    }

    // Crear un nuevo objeto Review con todos los campos
    return new Review(
        new EntityId(entity.getReviewId()),
        new EntityId(entity.getProductUserEntity().getUserId()),
        product,
        entity.getRating(),
        entity.getDescription()
    );
  }

  @Mapping(target = "reviewId", source = "id.value")
  @Mapping(target = "productUserEntity", ignore = true)
  @Mapping(source = "rating", target = "rating")
  @Mapping(source = "description", target = "description")
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract ReviewEntity toEntity(Review domain);
}