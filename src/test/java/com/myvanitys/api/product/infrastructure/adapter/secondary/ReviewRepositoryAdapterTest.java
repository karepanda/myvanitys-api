package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

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
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(productId)
          .userId(userId)
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");
      
      final Review review = Review.createWithExistingId(reviewEntityId,productUserEntityId,reviewDetails);
      
      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(5)
          .comment("Great product")
          .productUserId(productUserId)
          .build();

      when(jpaProductUserRepository.findByProductIdAndUserId(productId, userId))
          .thenReturn(Optional.of(productUserEntity));
      when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
      when(jpaReviewRepository.save(reviewEntity)).thenReturn(reviewEntity);
      when(reviewMapper.toDomain(any(ReviewEntity.class), any(EntityId.class))).thenReturn(review);

      // Act
      final Review result = target.save(review);

      // Assert
      assertThat(result).isEqualTo(review);
      verify(jpaReviewRepository).save(reviewEntity);
    }

    @Test
    void when_savingReviewThrowsDataAccessException_then_throwDatabaseException() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final UUID reviewId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId,productUserEntityId,reviewDetails);

      when(jpaProductUserRepository.findByProductIdAndUserId(any(), any()))
          .thenThrow(new DataAccessException("Test exception") {});

      // Act & Assert
      assertThatThrownBy(() -> target.save(review))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Error saving review");
    }

    @Test
    void when_productUserRelationNotFound_then_throwEntityNotFoundException() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final UUID reviewId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId,productUserEntityId,reviewDetails);

      when(jpaProductUserRepository.findByProductIdAndUserId(any(), any()))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> target.save(review))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Product-User relation not found");
    }
  }

  @Nested
  class FindById {

    @Test
    void when_reviewExists_then_returnReview() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(4)
          .comment("Good product")
          .productUserId(productUserId)
          .build();

      final ProductEntity productEntity = ProductEntity.builder()
          .productId(productId)
          .name("Test Product")
          .brand("Test Brand")
          .build();


      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review expectedReview = Review.createWithExistingId(reviewEntityId,productUserEntityId,reviewDetails);

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
      when(jpaProductRepository.findById(productId)).thenReturn(Optional.of(productEntity));
      when(reviewMapper.toDomain(any(), any())).thenReturn(expectedReview);

      // Act
      final Optional<Review> result = target.findById(new EntityId(reviewId));

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(expectedReview);
    }

    @Test
    void when_reviewDoesNotExist_then_returnEmptyOptional() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      // Act
      final Optional<Review> result = target.findById(new EntityId(reviewId));

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    void when_findByIdThrowsDataAccessException_then_throwDatabaseException() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();

      when(jpaReviewRepository.findById(reviewId))
          .thenThrow(new DataAccessException("Test exception") {});

      // Act & Assert
      assertThatThrownBy(() -> target.findById(new EntityId(reviewId)))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Error finding review");
    }
  }

  @Nested
  class DeleteById {

    @Test
    void when_validId_then_deleteSuccessfully() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();

      // Act
      target.deleteById(new EntityId(reviewId));

      // Assert
      verify(jpaReviewRepository).deleteById(reviewId);
    }

    @Test
    void when_deleteThrowsDataAccessException_then_throwDatabaseException() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();

      doThrow(new DataAccessException("Test exception") {})
          .when(jpaReviewRepository).deleteById(reviewId);

      // Act & Assert
      assertThatThrownBy(() -> target.deleteById(new EntityId(reviewId)))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Error deleting review");
    }
  }

  @Nested
  class FindByProductId {

    @Test
    void when_reviewsExist_then_returnReviewList() {
      // Arrange
      final UUID productId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

        final EntityId productEntityId = new EntityId(productId);
        final EntityId reviewEntityId = new EntityId(UUID.randomUUID());

      final ReviewEntity reviewEntity1 = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(4)
          .comment("Good product")
          .productUserId(productUserId)
          .build();

      final ProductEntity productEntity = ProductEntity.builder()
          .productId(productId)
          .name("Test Product")
          .build();

      final Product product = new Product(
          new EntityId(productId),
          "Test Product",
          "Test Brand",
          new Category(new EntityId(UUID.randomUUID()), "Test Category"),
          "#FFFFFF");

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId,productEntityId,reviewDetails);

      when(jpaReviewRepository.findByProductUserEntityProductId(productId))
          .thenReturn(List.of(reviewEntity1));
      when(jpaProductRepository.findById(productId))
          .thenReturn(Optional.of(productEntity));
      when(productMapper.toDomain(productEntity))
          .thenReturn(product);
      when(reviewMapper.toDomain(any(), any()))
          .thenReturn(review);

      // Act
      final List<Review> result = target.findByProductId(new EntityId(productId));

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.getFirst()).isEqualTo(review);
    }

    @Test
    void when_noReviewsExist_then_returnEmptyList() {
      // Arrange
      final UUID productId = UUID.randomUUID();

      when(jpaReviewRepository.findByProductUserEntityProductId(productId))
          .thenReturn(List.of());

      // Act
      final List<Review> result = target.findByProductId(new EntityId(productId));

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
      final UUID productId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();
      final UUID reviewId = UUID.randomUUID();

        final EntityId productUserEntityId = new EntityId(productId);
        final EntityId reviewEntityId = new EntityId(reviewId);


      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(UUID.randomUUID())
          .rating(3)
          .comment("Average product")
          .productUserId(productUserId)
          .build();

      final ProductEntity productEntity = ProductEntity.builder()
          .productId(productId)
          .name("Test Product")
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId,productUserEntityId,reviewDetails);

      when(jpaReviewRepository.findByProductUserEntityUserId(userId))
          .thenReturn(List.of(reviewEntity));
      when(jpaProductRepository.findById(productId))
          .thenReturn(Optional.of(productEntity));
      when(reviewMapper.toDomain(any(), any()))
          .thenReturn(review);

      // Act
      final List<Review> result = target.findByUserId(new EntityId(userId));

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.getFirst()).isEqualTo(review);
    }

    @Test
    void when_noReviewsExist_then_returnEmptyList() {
      // Arrange
      final UUID userId = UUID.randomUUID();

      when(jpaReviewRepository.findByProductUserEntityUserId(userId))
          .thenReturn(List.of());

      // Act
      final List<Review> result = target.findByUserId(new EntityId(userId));

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

      when(jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewId, userId))
          .thenReturn(true);

      // Act
      final boolean result = target.existsByReviewIdAndUserId(
          new EntityId(reviewId),
          new EntityId(userId)
      );

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void when_reviewDoesNotBelongToUser_then_returnFalse() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      when(jpaReviewRepository.existsByReviewIdAndProductUserEntityUserId(reviewId, userId))
          .thenReturn(false);

      // Act
      final boolean result = target.existsByReviewIdAndUserId(
          new EntityId(reviewId),
          new EntityId(userId)
      );

      // Assert
      assertThat(result).isFalse();
    }
  }
}