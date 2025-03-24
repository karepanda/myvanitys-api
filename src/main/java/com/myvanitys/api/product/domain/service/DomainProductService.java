package com.myvanitys.api.product.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service implementation for product operations
 */
@Service
public class DomainProductService implements ProductService {

  private final ProductRepository productRepository;

  private final ReviewRepository reviewRepository;

  private final ProductUserRepository productUserRepository;

  private final ProductMapper productMapper;

  private final ReviewMapper reviewMapper;

  public DomainProductService(
      ProductRepository productRepository,
      ReviewRepository reviewRepository,
      ProductUserRepository productUserRepository,
      ProductMapper productMapper,
      ReviewMapper reviewMapper) {
    this.productRepository = productRepository;
    this.reviewRepository = reviewRepository;
    this.productUserRepository = productUserRepository;
    this.productMapper = productMapper;
    this.reviewMapper = reviewMapper;
  }

  @Override
  @Transactional
  public Product createProduct(String name, String brand, Category category, String colorHex, EntityId userId) {
    // Generate a new product ID
    UUID productId = UUID.randomUUID();
    UUID userUuid = userId.getValue();

    // Create domain object with generated ID
    Product product = new Product(
        new EntityId(productId),
        name,
        brand,
        category,
        colorHex
    );

    // Convert to JPA entity and save
    ProductEntity productEntity = productMapper.toEntity(product);
    ProductEntity savedEntity = productRepository.save(productEntity);

    // Create product-user relationship
    productUserRepository.save(
        ProductUserEntity.builder()
            .productUserId(UUID.randomUUID())
            .productId(savedEntity.getProductId())
            .userId(userUuid)
            .build()
    );

    // Map back to domain object
    return productMapper.toDomain(savedEntity);
  }

  @Override
  @Transactional
  public Review addReviewToProduct(EntityId productId, String comment, int rating, EntityId userId) {
    UUID productUuid = productId.getValue();
    UUID userUuid = userId.getValue();

    // Verify product exists
    productRepository.findById(productUuid)
        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

    // Find the product-user relationship
    ProductUserEntity productUserEntity = productUserRepository.findByProductIdAndUserId(productUuid, userUuid);

    if (productUserEntity == null) {
      throw new IllegalArgumentException("User is not associated with this product");
    }

    // Create review entity with product-user reference
    ReviewEntity reviewEntity = ReviewEntity.builder()
        .reviewId(UUID.randomUUID())
        .comment(comment)
        .rating(rating)
        .productUserEntity(productUserEntity)
        .build();

    // Save review
    ReviewEntity savedReview = reviewRepository.save(reviewEntity);

    // Map to domain object and return
    return reviewMapper.toDomain(savedReview);
  }

  @Override
  @Transactional
  public Product updateProduct(EntityId productId, String name, String brand, Category category, String colorHex) {
    UUID productUuid = productId.getValue();

    // Get existing product
    ProductEntity existingEntity = productRepository.findById(productUuid)
        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

    // Update fields
    existingEntity.setName(name);
    existingEntity.setBrand(brand);
    if (category != null && category.categoryId() != null) {
      CategoryEntity categoryEntity = new CategoryEntity();
      categoryEntity.setCategoryId(category.categoryId().getValue());
      existingEntity.setCategory(categoryEntity);
    } else {
      existingEntity.setCategory(null);
    }
    existingEntity.setColorHex(colorHex);

    // Save updated entity
    ProductEntity updatedEntity = productRepository.save(existingEntity);

    // Map to domain and return
    return productMapper.toDomain(updatedEntity);
  }

  @Override
  @Transactional
  public void deleteProduct(EntityId productId) {
    UUID productUuid = productId.getValue();

    // Delete related reviews first
    List<ProductUserEntity> productUserEntities = productUserRepository.findByProductId(productUuid);
    for (ProductUserEntity productUserEntity : productUserEntities) {
      // Delete reviews associated with this product-user relationship
      List<ReviewEntity> reviews = reviewRepository.findByProductUserEntityProductUserId(productUserEntity.getProductUserId());
      reviewRepository.deleteAll(reviews);
    }

    // Delete product-user relationships
    productUserRepository.deleteByProductId(productUuid);

    // Delete the product
    productRepository.deleteById(productUuid);
  }

  @Override
  public Optional<Product> getProductById(EntityId productId) {
    UUID productUuid = productId.getValue();

    return productRepository.findById(productUuid)
        .map(productMapper::toDomain);
  }

  @Override
  public List<Product> getProductsByUserId(EntityId userId) {
    UUID userUuid = userId.getValue();

    // Get products through product-user relationships
    return productUserRepository.findByUserId(userUuid).stream()
        .map(ProductUserEntity::getProductId)
        .map(productRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(productMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Review updateReview(EntityId reviewId, String comment, int rating) {
    UUID reviewUuid = reviewId.getValue();

    // Get existing review
    ReviewEntity existingEntity = reviewRepository.findById(reviewUuid)
        .orElseThrow(() -> new IllegalArgumentException("Review not found"));

    // Update fields
    existingEntity.setComment(comment);
    existingEntity.setRating(rating);

    // Save updated entity
    ReviewEntity updatedEntity = reviewRepository.save(existingEntity);

    // Map to domain and return
    return reviewMapper.toDomain(updatedEntity);
  }

  @Override
  @Transactional
  public void deleteReview(EntityId reviewId) {
    UUID reviewUuid = reviewId.getValue();

    // Delete the review
    reviewRepository.deleteById(reviewUuid);
  }

  @Override
  public List<Review> getReviewsByProductId(EntityId productId) {
    UUID productUuid = productId.getValue();

    return reviewRepository.findByProductUserEntityProductId(productUuid).stream()
        .map(reviewMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isProductOwner(EntityId productId, EntityId userId) {
    UUID productUuid = productId.getValue();
    UUID userUuid = userId.getValue();

    return productUserRepository.existsByProductIdAndUserId(productUuid, userUuid);
  }

  @Override
  public boolean isReviewOwner(EntityId reviewId, EntityId userId) {
    UUID reviewUuid = reviewId.getValue();
    UUID userUuid = userId.getValue();

    return reviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewUuid, userUuid);
  }
}