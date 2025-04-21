package com.myvanitys.api.product.infrastructure.adapter.secondary.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.adapter.secondary.ReviewRepositoryAdapter;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewRepositoryAdapterTest {

  @Mock
  private JpaReviewRepository jpaReviewRepository;

  @Mock
  private JpaProductUserRepository jpaProductUserRepository;

  @Mock
  private JpaProductRepository jpaProductRepository;

  @Mock
  private ReviewMapper reviewMapper;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private ReviewRepositoryAdapter target;

  @Nested
  class Save {

    @Test
    void when_validReview_then_saveSuccessfully() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID reviewId = UUID.randomUUID();

      final EntityId productEntityId = new EntityId(productId);
      final EntityId userEntityId = new EntityId(userId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final Product product = new Product(
          productEntityId,
          "Test Product",
          "Test Brand",
          new Category(new EntityId(UUID.randomUUID()), "Test Category"),
          "#FFFFFF");

      final Review review = new Review(
          reviewEntityId,
          userEntityId,
          product,
          5,
          "Great product"
      );

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(UUID.randomUUID())
          .productId(productId)
          .userId(userId)
          .build();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(5)
          .comment("Great product")
          .build();

      final ReviewEntity savedEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(5)
          .comment("Great product")
          .productUserEntity(productUserEntity)
          .build();

      when(jpaProductUserRepository.findByProductIdAndUserId(productId, userId))
          .thenReturn(Optional.of(productUserEntity));
      when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
      when(jpaReviewRepository.save(reviewEntity)).thenReturn(savedEntity);
      when(reviewMapper.toDomain(savedEntity, product)).thenReturn(review);

      // Act
      final Review result = target.save(review);

      // Assert
      assertThat(result).isEqualTo(review);

      // Verificar que se guardó la entidad con las propiedades correctas
      ArgumentCaptor<ReviewEntity> entityCaptor = ArgumentCaptor.forClass(ReviewEntity.class);
      verify(jpaReviewRepository).save(entityCaptor.capture());

      ReviewEntity capturedEntity = entityCaptor.getValue();
      assertThat(capturedEntity.getProductUserEntity()).isEqualTo(productUserEntity);
    }

    @Test
    void when_productUserRelationNotFound_then_throwEntityNotFoundException() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID reviewId = UUID.randomUUID();

      final EntityId productEntityId = new EntityId(productId);
      final EntityId userEntityId = new EntityId(userId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final Product product = new Product(
          productEntityId,
          "Test Product",
          "Test Brand",
          new Category(new EntityId(UUID.randomUUID()), "Test Category"),
          "#FFFFFF");

      final Review review = new Review(
          reviewEntityId,
          userEntityId,
          product,
          5,
          "Great product"
      );

      when(jpaProductUserRepository.findByProductIdAndUserId(productId, userId))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> target.save(review))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  class FindById {

    @Test
    void when_reviewExists_then_returnReview() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productId = UUID.randomUUID();
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(UUID.randomUUID())
          .productId(productId)
          .userId(UUID.randomUUID())
          .build();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(4)
          .comment("Good product")
          .productUserEntity(productUserEntity)
          .build();

      final ProductEntity productEntity = ProductEntity.builder()
          .productId(productId)
          .name("Test Product")
          .brand("Test Brand")
          .build();

      final Product product = new Product(
          new EntityId(productId),
          "Test Product",
          "Test Brand",
          new Category(new EntityId(UUID.randomUUID()), "Test Category"),
          "#FFFFFF");

      final Review expectedReview = new Review(
          reviewEntityId,
          new EntityId(UUID.randomUUID()),
          product,
          4,
          "Good product"
      );

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(product);
      when(reviewMapper.toDomain(reviewEntity, product)).thenReturn(expectedReview);

      // Act
      final Optional<Review> result = target.findById(reviewEntityId);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(expectedReview);
    }

    @Test
    void when_reviewDoesNotExist_then_returnEmptyOptional() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final EntityId reviewEntityId = new EntityId(reviewId);

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      // Act
      final Optional<Review> result = target.findById(reviewEntityId);

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class DeleteById {

    @Test
    void when_validId_then_deleteSuccessfully() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final EntityId reviewEntityId = new EntityId(reviewId);

      // Act
      target.deleteById(reviewEntityId);

      // Assert
      verify(jpaReviewRepository).deleteById(reviewId);
    }
  }

  @Nested
  class FindByProductId {

    @Test
    void when_reviewsExist_then_returnReviewList() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);

      final ReviewEntity reviewEntity1 = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(4)
          .comment("Good product")
          .build();

      final ReviewEntity reviewEntity2 = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(5)
          .comment("Excellent product")
          .build();

      final List<ReviewEntity> reviewEntities = Arrays.asList(reviewEntity1, reviewEntity2);

      final Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

      final Product product = new Product(
          productEntityId,
          "Test Product",
          "Test Brand",
          category,
          "#FFFFFF");

      final ProductEntity productEntity = ProductEntity.builder()
          .productId(productId)
          .name("Test Product")
          .brand("Test Brand")
          .build();

      final Review review1 = new Review(
          new EntityId(reviewEntity1.getReviewId()),
          new EntityId(UUID.randomUUID()),
          product,
          4,
          "Good product"
      );

      final Review review2 = new Review(
          new EntityId(reviewEntity2.getReviewId()),
          new EntityId(UUID.randomUUID()),
          product,
          5,
          "Excellent product"
      );

      when(jpaReviewRepository.findByProductUserEntityProductId(productId)).thenReturn(reviewEntities);
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(productMapper.toDomain(productEntity)).thenReturn(product);
      when(reviewMapper.toDomain(reviewEntity1, product)).thenReturn(review1);
      when(reviewMapper.toDomain(reviewEntity2, product)).thenReturn(review2);

      // Act
      final List<Review> result = target.findByProductId(productEntityId);

      // Assert
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(review1, review2);
    }

    @Test
    void when_noReviewsExist_then_returnEmptyList() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final EntityId productEntityId = new EntityId(productId);

      when(jpaReviewRepository.findByProductUserEntityProductId(productId)).thenReturn(List.of());

      // Act
      final List<Review> result = target.findByProductId(productEntityId);

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class FindByUserId {

    @Test
    void when_reviewsExist_then_returnReviewList() {
      // Arrange
      final UUID userId = UUID.randomUUID();
      final EntityId userEntityId = new EntityId(userId);

      final UUID productId1 = UUID.randomUUID();
      final UUID productId2 = UUID.randomUUID();

      // Crear instancias de ProductUserEntity
      final ProductUserEntity productUserEntity1 = ProductUserEntity.builder()
          .productUserId(UUID.randomUUID())
          .productId(productId1)
          .userId(userId)
          .build();

      final ProductUserEntity productUserEntity2 = ProductUserEntity.builder()
          .productUserId(UUID.randomUUID())
          .productId(productId2)
          .userId(userId)
          .build();

      // Crear entidades ReviewEntity con sus respectivos ProductUserEntity
      final ReviewEntity reviewEntity1 = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(3)
          .comment("Average product")
          .productUserEntity(productUserEntity1)  // Asignar productUserEntity1
          .build();

      final ReviewEntity reviewEntity2 = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(2)
          .comment("Below average product")
          .productUserEntity(productUserEntity2)  // Asignar productUserEntity2
          .build();

      final List<ReviewEntity> reviewEntities = Arrays.asList(reviewEntity1, reviewEntity2);

      final Category category = new Category(new EntityId(UUID.randomUUID()), "Test Category");

      final Product product1 = new Product(
          new EntityId(productId1),
          "Test Product 1",
          "Test Brand 1",
          category,
          "#FFFFFF");

      final Product product2 = new Product(
          new EntityId(productId2),
          "Test Product 2",
          "Test Brand 2",
          category,
          "#000000");

      // Configurar entidades de producto
      final ProductEntity productEntity1 = ProductEntity.builder()
          .productId(productId1)
          .name("Test Product 1")
          .brand("Test Brand 1")
          .build();

      final ProductEntity productEntity2 = ProductEntity.builder()
          .productId(productId2)
          .name("Test Product 2")
          .brand("Test Brand 2")
          .build();

      final Review review1 = new Review(
          new EntityId(reviewEntity1.getReviewId()),
          userEntityId,
          product1,
          3,
          "Average product"
      );

      final Review review2 = new Review(
          new EntityId(reviewEntity2.getReviewId()),
          userEntityId,
          product2,
          2,
          "Below average product"
      );

      // Configurar mocks
      when(jpaReviewRepository.findByProductUserEntityUserId(userId)).thenReturn(reviewEntities);

      // Configurar búsqueda de productos
      when(jpaProductRepository.findById(productId1)).thenReturn(Optional.of(productEntity1));
      when(jpaProductRepository.findById(productId2)).thenReturn(Optional.of(productEntity2));
      when(productMapper.toDomain(productEntity1)).thenReturn(product1);
      when(productMapper.toDomain(productEntity2)).thenReturn(product2);

      // Configurar mapper para reviews
      when(reviewMapper.toDomain(reviewEntity1, product1)).thenReturn(review1);
      when(reviewMapper.toDomain(reviewEntity2, product2)).thenReturn(review2);

      // Act
      final List<Review> result = target.findByUserId(userEntityId);

      // Assert
      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(review1, review2);
    }

    @Test
    void when_noReviewsExist_then_returnEmptyList() {
      // Arrange
      final UUID userId = UUID.randomUUID();
      final EntityId userEntityId = new EntityId(userId);

      when(jpaReviewRepository.findByProductUserEntityUserId(userId)).thenReturn(List.of());

      // Act
      final List<Review> result = target.findByUserId(userEntityId);

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class ExistsByReviewIdAndUserId {

    @Test
    void when_reviewBelongsToUser_then_returnTrue() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final EntityId reviewEntityId = new EntityId(reviewId);
      final EntityId userEntityId = new EntityId(userId);

      when(jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewId, userId)).thenReturn(true);

      // Act
      final boolean result = target.existsByReviewIdAndUserId(reviewEntityId, userEntityId);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void when_reviewDoesNotBelongToUser_then_returnFalse() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final EntityId reviewEntityId = new EntityId(reviewId);
      final EntityId userEntityId = new EntityId(userId);

      when(jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewId, userId)).thenReturn(false);

      // Act
      final boolean result = target.existsByReviewIdAndUserId(reviewEntityId, userEntityId);

      // Assert
      assertThat(result).isFalse();
    }
  }
}
