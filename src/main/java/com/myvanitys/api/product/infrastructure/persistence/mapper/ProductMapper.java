package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {EntityIdMapper.class, CategoryMapper.class})
public class ProductMapper {

  /**
   * Convierte de ProductEntity a Product del dominio. La categoría debe ser proporcionada externamente si es necesaria.
   */
  public Product toDomain(ProductEntity productEntity, Category category) {
    if (productEntity == null) {
      return null;
    }

    EntityId id = new EntityId(productEntity.getProductId());

    return new Product(
        id,
        productEntity.getName(),
        productEntity.getBrand(),
        category,
        productEntity.getColorHex()
    );
  }

  /**
   * Sobrecarga para cuando no se dispone de la categoría. El objeto Product se creará sin categoría y será responsabilidad del código
   * cliente asignarla posteriormente si es necesario.
   */
  public Product toDomain(ProductEntity productEntity) {
    if (productEntity == null) {
      return null;
    }

    EntityId id = new EntityId(productEntity.getProductId());

    return new Product(
        id,
        productEntity.getName(),
        productEntity.getBrand(),
        null, // Sin categoría
        productEntity.getColorHex()
    );
  }

  /**
   * Convierte de Product a ProductEntity
   */
  public ProductEntity toEntity(Product product) {
    if (product == null) {
      return null;
    }

    ProductEntity entity = new ProductEntity();
    entity.setProductId(product.getId().getValue());
    entity.setName(product.getName());
    entity.setBrand(product.getBrand());
    entity.setColorHex(product.getColorHex());

    if (product.getCategory() != null) {
      entity.setCategoryId(product.getCategory().categoryId().getValue());
    }

    return entity;
  }

  /**
   * Métodos para convertir listas
   */
  public List<Product> toDomainList(List<ProductEntity> productEntities) {
    if (productEntities == null) {
      return new ArrayList<>();
    }

    List<Product> result = new ArrayList<>(productEntities.size());
    for (ProductEntity entity : productEntities) {
      result.add(toDomain(entity));
    }
    return result;
  }

  public List<ProductEntity> toEntityList(List<Product> products) {
    if (products == null) {
      return new ArrayList<>();
    }

    List<ProductEntity> result = new ArrayList<>(products.size());
    for (Product product : products) {
      result.add(toEntity(product));
    }
    return result;
  }

  /**
   * Convierte un ProductEntity a Product con todas sus relaciones
   */
  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers, Category category) {
    Product product = toDomain(productEntity, category);

    if (productUsers != null && !productUsers.isEmpty()) {
      for (ProductUserEntity pu : productUsers) {
        ProductUserRelation relation = toProductUserRelation(pu);
        product.getUserRelations().add(relation);

        // Reviews Mapping
        if (pu.getReviews() != null && !pu.getReviews().isEmpty()) {
          for (ReviewEntity reviewEntity : pu.getReviews()) {
            Review review = toReview(reviewEntity, product);
            product.addReview(review);
          }
        }
      }
    }

    return product;
  }

  /**
   * Sobrecarga para cuando no se dispone de la categoría
   */
  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers) {
    return toDomainWithRelations(productEntity, productUsers, null);
  }

  /**
   * Convierte de ProductUserEntity a ProductUserRelation
   */
  public ProductUserRelation toProductUserRelation(ProductUserEntity productUserEntity) {
    if (productUserEntity == null) {
      return null;
    }

    EntityId reviewId = null;
    if (productUserEntity.getReviews() != null && !productUserEntity.getReviews().isEmpty()) {
      // Si hay reviews, tomamos el ID de la primera
      reviewId = new EntityId(productUserEntity.getReviews().get(0).getReviewId());
    }

    return new ProductUserRelation(
        new EntityId(productUserEntity.getProductUserId()),
        new EntityId(productUserEntity.getProductId()),
        new EntityId(productUserEntity.getUserId()),
        reviewId
    );
  }

  /**
   * Convierte de ReviewEntity a Review
   */
  public Review toReview(ReviewEntity reviewEntity, Product product) {
    if (reviewEntity == null || product == null) {
      return null;
    }

    return new Review(
        new EntityId(reviewEntity.getReviewId()),
        new EntityId(reviewEntity.getProductUserEntity().getUserId()),
        product,
        reviewEntity.getRating(),
        reviewEntity.getComment()
    );
  }

  /**
   * Convierte de Review a ReviewEntity
   */
  public ReviewEntity toReviewEntity(Review review, ProductUserEntity productUserEntity) {
    if (review == null || productUserEntity == null) {
      return null;
    }

    return ReviewEntity.builder()
        .reviewId(review.getId().getValue())
        .rating(review.getRating())
        .comment(review.getComment())
        .productUserEntity(productUserEntity)
        .build();
  }
}
