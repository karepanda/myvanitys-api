package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Review del dominio y ReviewEntity de JPA
 */
@Component
public class ReviewMapper {

  /**
   * Convierte una ReviewEntity a un objeto Review del dominio
   *
   * @param entity La entidad a convertir
   * @param product El producto asociado a la review
   * @return El objeto de dominio Review
   */
  public Review toDomain(ReviewEntity entity, Product product) {
    if (entity == null) {
      return null;
    }

    // Validar que el producto no sea nulo
    if (product == null) {
      throw new IllegalArgumentException("Product cannot be null for review conversion");
    }

    // Obtener los IDs necesarios
    EntityId reviewId = new EntityId(entity.getReviewId());
    EntityId userId = new EntityId(entity.getProductUserEntity().getUserId());

    // Crear y retornar el objeto de dominio
    return new Review(
        reviewId,
        userId,
        product,
        entity.getRating(),
        entity.getComment()
    );
  }

  /**
   * Convierte un objeto Review del dominio a una ReviewEntity
   *
   * @param domain El objeto de dominio a convertir
   * @return La entidad ReviewEntity
   */
  public ReviewEntity toEntity(Review domain) {
    if (domain == null) {
      return null;
    }

    // Obtener el UUID de la review
    UUID reviewId = domain.getId() != null ? domain.getId().getValue() : null;

    // Crear la entidad
    return ReviewEntity.builder()
        .reviewId(reviewId)
        .rating(domain.getRating())
        .comment(domain.getComment())
        // No establecemos la relación productUserEntity aquí
        // porque se maneja en el adaptador
        .build();
  }
}