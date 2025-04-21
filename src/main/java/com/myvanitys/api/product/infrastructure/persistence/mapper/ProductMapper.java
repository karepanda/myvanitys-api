package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
   * Converts a ProductEntity to a domain Product. The category must be provided externally if needed.
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
   * Overload for cases where the category is not available. The Product will be created without a category, and it is the client's
   * responsibility to assign it later if needed.
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
        null, // No category
        productEntity.getColorHex()
    );
  }

  /**
   * Converts a domain Product to a ProductEntity.
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
   * Methods for converting lists.
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
    return Optional.ofNullable(products)
        .map(p -> p.stream()
            .map(this::toEntity)
            .toList())
        .orElseGet(ArrayList::new);
  }

  /**
   * Converts a ProductEntity to a Product with all its relationships.
   */
  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers, Category category) {
    // Create the main product
    Product product = toDomain(productEntity, category);

    // Use Optional to avoid explicit null checks and make it cleaner
    Optional.ofNullable(productUsers)
        .ifPresent(users -> users.forEach(pu -> {
          // Add the user relation
          product.getUserRelations().add(toProductUserRelation(pu));

          // Map reviews if they exist
          Optional.ofNullable(pu.getReviews()).ifPresent(reviews -> reviews.forEach(reviewEntity -> {
            Review review = toReview(reviewEntity, product);
            product.addReview(review);
          }));
        }));

    return product;
  }

  /**
   * Overload for when the category is not available.
   */
  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers) {
    return toDomainWithRelations(productEntity, productUsers, null);
  }

  /**
   * Converts a ProductUserEntity to a ProductUserRelation.
   */
  public ProductUserRelation toProductUserRelation(ProductUserEntity productUserEntity) {
    if (productUserEntity == null) {
      return null;
    }

    EntityId reviewId = null;
    if (productUserEntity.getReviews() != null && !productUserEntity.getReviews().isEmpty()) {
      // If there are reviews, use the ID of the first one
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
   * Converts a ReviewEntity to a domain Review.
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
   * Converts a domain Review to a ReviewEntity.
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
