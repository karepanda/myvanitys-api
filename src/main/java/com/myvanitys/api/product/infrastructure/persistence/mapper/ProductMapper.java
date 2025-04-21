package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.List;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class, CategoryMapper.class})
public abstract class ProductMapper {

  // Mapping from ProductEntity to Product
  @Mapping(target = "id", source = "productId")
  @Mapping(target = "category", source = "category") // Utilizará CategoryMapper automáticamente
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "userRelations", ignore = true)
  @Mapping(target = "averageRating", ignore = true)
  public abstract Product toDomain(ProductEntity productEntity);

  // Mapping from Product to ProductEntity
  @Mapping(target = "productId", source = "id.value")
  @Mapping(target = "category", source = "category") // Utilizará CategoryMapper automáticamente
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  public abstract ProductEntity toEntity(Product product);

  // Métodos para convertir listas
  public abstract List<Product> toDomainList(List<ProductEntity> productEntities);

  public abstract List<ProductEntity> toEntityList(List<Product> products);

  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers) {
    Product product = toDomain(productEntity);

    if (productUsers != null) {
      // ProductUserRelation Mapping
      productUsers.forEach(pu -> {
        ProductUserRelation relation = toProductUserRelation(pu);
        product.getUserRelations().add(relation);

        // Reviews Mapping
        if (pu.getReviews() != null) {
          pu.getReviews().forEach(reviewEntity -> {
            Review review = toReview(reviewEntity, product);
            product.addReview(review);
          });
        }
      });
    }

    return product;
  }

  protected ProductUserRelation toProductUserRelation(ProductUserEntity productUserEntity) {
    EntityId reviewId = null;
    if (productUserEntity.getReviews() != null && !productUserEntity.getReviews().isEmpty()) {
      // If there are reviews, we take the ID of the first one (or apply the logic you need).
      reviewId = new EntityId(productUserEntity.getReviews().getFirst().getReviewId());
    }

    return new ProductUserRelation(
        new EntityId(productUserEntity.getProductUserId()),
        new EntityId(productUserEntity.getProductId()),
        new EntityId(productUserEntity.getUserId()),
        reviewId
    );
  }

  protected Review toReview(ReviewEntity reviewEntity, Product product) {
    return new Review(
        new EntityId(reviewEntity.getReviewId()),
        new EntityId(reviewEntity.getProductUserEntity().getUserId()),
        product,
        reviewEntity.getRating(),
        reviewEntity.getComment()
    );
  }

  //  Review to ReviewEntity mapping (needs associated ProductUserEntity)
  public ReviewEntity toReviewEntity(Review review, ProductUserEntity productUserEntity) {
    return ReviewEntity.builder()
        .reviewId(review.getId().getValue())
        .rating(review.getRating())
        .comment(review.getComment())
        .productUserEntity(productUserEntity)
        .build();
  }
}