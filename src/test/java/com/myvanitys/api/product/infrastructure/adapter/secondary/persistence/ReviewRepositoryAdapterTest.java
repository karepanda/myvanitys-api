package com.myvanitys.api.product.infrastructure.adapter.secondary.persistence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.secondary.ReviewRepositoryAdapter;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewRepositoryAdapterTest {

  @InjectMocks
  private ReviewRepositoryAdapter target;

  @Mock
  private JpaReviewRepository jpaReviewRepository;

  @Mock
  private JpaProductUserRepository jpaProductUserRepository;

  @Mock
  private ReviewMapper reviewMapper;

  private EntityId productId;

  private EntityId userId;

  private EntityId reviewId;

  private Product product;

  private Review review;

  private ReviewEntity reviewEntity;

  private ProductUserEntity productUserEntity;

  @BeforeEach
  void setUp() {
    // Initialize IDs
    productId = new EntityId(UUID.randomUUID());
    EntityId categoryId = new EntityId(UUID.randomUUID());
    userId = new EntityId(UUID.randomUUID());
    reviewId = new EntityId(UUID.randomUUID());

    // Initialize domain objects
    Category category = new Category(categoryId, "Test Category");
    product = new Product(productId, "Test Product", "Test Brand", category, "#FFFFFF");
    review = new Review(reviewId, userId, product, 5, "Great product!");

    // Initialize entities
    productUserEntity = new ProductUserEntity();
    productUserEntity.setProductUserId(UUID.randomUUID());
    productUserEntity.setProductId(productId.getValue());
    productUserEntity.setUserId(userId.getValue());

    reviewEntity = new ReviewEntity();
    reviewEntity.setReviewId(reviewId.getValue());
    reviewEntity.setRating(5);
    reviewEntity.setComment("Great product!");
    reviewEntity.setProductUserEntity(productUserEntity);
    reviewEntity.setCreatedAt(Instant.now());
    reviewEntity.setUpdatedAt(Instant.now());
  }

  @Nested
  class Save {

    @Test
    void when_givenValidReview_then_reviewIsSaved() {
      when(jpaProductUserRepository.findByProductIdAndUserId(
          product.getId().getValue(),
          userId.getValue()))
          .thenReturn(productUserEntity);

      when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
      when(jpaReviewRepository.save(reviewEntity)).thenReturn(reviewEntity);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      final Review result = target.save(review);

      assertThat(result).isEqualTo(review);
      verify(jpaProductUserRepository).findByProductIdAndUserId(
          product.getId().getValue(),
          userId.getValue());
      verify(reviewMapper).toEntity(review);
      verify(jpaReviewRepository).save(reviewEntity);
      verify(reviewMapper).toDomain(reviewEntity);
    }
  }

  @Nested
  class FindById {

    @Test
    void when_givenExistingId_then_reviewIsReturned() {
      when(jpaReviewRepository.findById(reviewId.getValue())).thenReturn(Optional.of(reviewEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      final Optional<Review> result = target.findById(reviewId);

      assertThat(result).isPresent().contains(review);
      verify(jpaReviewRepository).findById(reviewId.getValue());
      verify(reviewMapper).toDomain(reviewEntity);
    }

    @Test
    void when_givenNonExistingId_then_emptyOptionalIsReturned() {
      when(jpaReviewRepository.findById(reviewId.getValue())).thenReturn(Optional.empty());

      final Optional<Review> result = target.findById(reviewId);

      assertThat(result).isEmpty();
      verify(jpaReviewRepository).findById(reviewId.getValue());
      verify(reviewMapper, never()).toDomain(any());
    }
  }

  @Nested
  class FindByProductId {

    @Test
    void when_givenProductId_then_returnReviewsList() {
      List<ReviewEntity> reviewEntities = List.of(reviewEntity);
      when(jpaReviewRepository.findByProductUserEntityProductId(productId.getValue()))
          .thenReturn(reviewEntities);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      final List<Review> result = target.findByProductId(productId);

      assertThat(result).isEqualTo(List.of(review));
      verify(jpaReviewRepository).findByProductUserEntityProductId(productId.getValue());
      verify(reviewMapper).toDomain(reviewEntity);
    }
  }

  @Nested
  class DeleteById {

    @Test
    void when_givenReviewId_then_reviewIsDeleted() {
      target.deleteById(reviewId);

      verify(jpaReviewRepository).deleteById(reviewId.getValue());
    }
  }
}