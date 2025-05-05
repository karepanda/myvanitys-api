package com.myvanitys.api.product.application.usecase;

import java.util.List;
import java.util.Objects;

import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.exception.CategoryNotFoundException;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.port.secondary.ReviewRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProductByUser implements FindProductUserUseCase {

  private final JpaProductRepository jpaProductRepository;

  private final ProductMapper productMapper;

  private final CategoryRepository categoryRepository;

  private final ReviewRepository reviewRepository;

  private final ReviewMapper reviewMapper;

  @Override
  @Transactional(readOnly = true)
  public List<Product> query(FindProductUserQuery query) {
    List<ProductEntity> productEntities = jpaProductRepository.findByUserId(query.userId().getValue());

    List<Product> products = productEntities.stream()
        .map(productEntity -> {
          final Category category = getCategory(productEntity);
          final List<Review> reviews = getProductReview(new EntityId(productEntity.getProductId()));
          return productMapper.toDomain(productEntity, category, reviews);
        })
        .filter(Objects::nonNull)
        .toList();

    if (products.isEmpty()) {
      throw ProductNotFoundException.forUser(query.userId());
    }

    return products;

  }

  private Category getCategory(ProductEntity entity) {
    return categoryRepository.findById(new EntityId(entity.getCategoryId()))
        .orElseThrow(() -> new CategoryNotFoundException("Category not found for product: " + entity.getProductId()));
  }

  private List<Review> getProductReview(EntityId productId) {
    return reviewRepository.findByProductId(productId).stream()
        .toList();
  }
}