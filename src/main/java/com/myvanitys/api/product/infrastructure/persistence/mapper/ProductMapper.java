package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {EntityIdMapper.class, CategoryMapper.class})
public class ProductMapper {

  public Product toDomain(ProductEntity productEntity, Category category) {
    if (productEntity == null) {
      return null;
    }

    Objects.requireNonNull(category, "Category cannot be null");
    EntityId id = new EntityId(productEntity.getProductId());

    return Product.reconstruct(
        id,
        productEntity.getName(),
        productEntity.getBrand(),
        category,
        productEntity.getColorHex(),
        null,
        null
    );
  }

  public Product toNewDomainProduct(ProductEntity productEntity) {
    if (productEntity == null) {
      return null;
    }

    return Product.newProduct(
        productEntity.getName(),
        productEntity.getBrand(),
        productEntity.getColorHex()
    );
  }

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

  public List<Product> toDomainList(List<ProductEntity> productEntities, Category category) {
    Objects.requireNonNull(category, "Category cannot be null");

    return productEntities == null
        ? List.of()
        : productEntities.stream()
            .map(entity -> toDomain(entity, category))
            .toList();
  }

  public List<Product> toNewDomainProductList(List<ProductEntity> productEntities) {
    return productEntities == null
        ? List.of()
        : productEntities.stream()
            .map(this::toNewDomainProduct)
            .toList();
  }

  public List<ProductEntity> toEntityList(List<Product> products) {
    return products == null
        ? List.of()
        : products.stream()
            .map(this::toEntity)
            .toList();
  }

  public Product toDomainWithRelations(ProductEntity productEntity, List<ProductUserEntity> productUsers, Category category) {
    if (productEntity == null) {
      return null;
    }

    Objects.requireNonNull(category, "Category cannot be null");
    EntityId id = new EntityId(productEntity.getProductId());

    if (productUsers == null || productUsers.isEmpty()) {
      return Product.reconstruct(
          id,
          productEntity.getName(),
          productEntity.getBrand(),
          category,
          productEntity.getColorHex(),
          null,
          null
      );
    }

    Set<ProductUserRelation> relations = new HashSet<>();
    List<Review> reviews = new ArrayList<>();

    productUsers.forEach(pu -> {
      ProductUserRelation relation = toProductUserRelation(pu);
      relations.add(relation);

      if (pu.getReviews() != null) {
        pu.getReviews().stream()
            .map(reviewEntity -> toReview(reviewEntity, relation.getId()))
            .filter(Objects::nonNull)
            .forEach(reviews::add);
      }
    });

    return Product.reconstruct(
        id,
        productEntity.getName(),
        productEntity.getBrand(),
        category,
        productEntity.getColorHex(),
        reviews,
        relations
    );
  }

  public ProductUserRelation toProductUserRelation(ProductUserEntity productUserEntity) {
    if (productUserEntity == null) {
      return null;
    }

    EntityId id = new EntityId(productUserEntity.getProductUserId());
    EntityId productId = new EntityId(productUserEntity.getProductId());
    EntityId userId = new EntityId(productUserEntity.getUserId());

    EntityId reviewId = null;
    if (productUserEntity.getReviews() != null && !productUserEntity.getReviews().isEmpty()) {
      reviewId = new EntityId(productUserEntity.getReviews().getFirst().getReviewId());
    }

    return ProductUserRelation.reconstruct(id, productId, userId, reviewId);
  }

  public Review toReview(ReviewEntity reviewEntity, EntityId productUserId) {
    if (reviewEntity == null || productUserId == null) {
      return null;
    }

    ReviewDetails reviewDetails = ReviewDetails.of(
        reviewEntity.getRating(),
        reviewEntity.getComment(),
        reviewEntity.getCreatedAt(),
        reviewEntity.getUpdatedAt() != null ? reviewEntity.getUpdatedAt() : reviewEntity.getCreatedAt(),
        reviewEntity.getDeletedAt()
    );

    return Review.createWithExistingId(
        new EntityId(reviewEntity.getReviewId()),
        productUserId,
        reviewDetails
    );
  }

  public ReviewEntity toReviewEntity(Review review) {
    if (review == null) {
      return null;
    }

    return ReviewEntity.builder()
        .reviewId(review.getId().getValue())
        .rating(review.getRating())
        .comment(review.getComment())
        .productUserId(review.getProductUserId().getValue())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .deletedAt(review.getDeletedAt())
        .build();
  }

  public List<ProductUserEntity> toProductUserEntityList(Product product) {
    if (product == null) {
      return List.of();
    }

    Map<EntityId, ProductUserEntity> productUserMap = new HashMap<>();

    // Carer todas las entidades ProductUser basadas en las relaciones
    for (ProductUserRelation relation : product.getUserRelations()) {
      ProductUserEntity productUserEntity = new ProductUserEntity();
      productUserEntity.setProductUserId(relation.getId().getValue());
      productUserEntity.setProductId(relation.getProductId().getValue());
      productUserEntity.setUserId(relation.getUserId().getValue());
      productUserEntity.setReviews(new ArrayList<>());

      productUserMap.put(relation.getId(), productUserEntity);
    }

    // Añadir las reviews a las entidades ProductUser correspondientes
    for (Review review : product.getReviews()) {
      EntityId productUserId = review.getProductUserId();
      ProductUserEntity productUserEntity = productUserMap.get(productUserId);

      if (productUserEntity != null) {
        ReviewEntity reviewEntity = toReviewEntity(review);
        productUserEntity.getReviews().add(reviewEntity);
      }
    }

    return new ArrayList<>(productUserMap.values());
  }
}