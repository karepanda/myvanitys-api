package com.myvanitys.api.product.infrastructure.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.exception.DatabaseException;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ReviewMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityNotFoundException;
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
  private ReviewMapper reviewMapper;

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

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(productId)
          .userId(userId)
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(5)
          .comment("Great product")
          .productUserId(productUserId)
          .build();

      // Configurar el mock para findById de productUserId
      when(jpaProductUserRepository.findById(productUserId))
          .thenReturn(Optional.of(productUserEntity));

      when(reviewMapper.toEntity(review)).thenReturn(reviewEntity);
      when(jpaReviewRepository.save(reviewEntity)).thenReturn(reviewEntity);
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

      // Act
      final Review result = target.save(review);

      // Assert
      assertThat(result).isEqualTo(review);
      verify(jpaReviewRepository).save(reviewEntity);
    }

    @Test
    void when_savingReviewThrowsDataAccessException_then_throwDatabaseException() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      when(jpaProductUserRepository.findById(productUserId))
          .thenThrow(new DataAccessException("Test exception") {
          });

      // Act & Assert
      assertThatThrownBy(() -> target.save(review))
          .isInstanceOf(DatabaseException.class)
          .hasMessageContaining("Error saving review");
    }

    @Test
    void when_productUserRelationNotFound_then_throwEntityNotFoundException() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      when(jpaProductUserRepository.findById(productUserId))
          .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> target.save(review))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("ProductUser relation not found for review");
    }
  }

  @Nested
  class FindById {

    @Test
    void when_reviewExists_then_returnReview() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(4)
          .comment("Good product")
          .productUserId(productUserId)
          .build();

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(UUID.randomUUID())
          .userId(UUID.randomUUID())
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");

      final Review expectedReview = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
      when(jpaProductUserRepository.findById(productUserId)).thenReturn(Optional.of(productUserEntity));
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(expectedReview);

      // Act
      final Optional<Review> result = target.findById(new EntityId(reviewId));

      // Assert
      assertThat(result)
          .isPresent()
          .contains(expectedReview);
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
          .thenThrow(new DataAccessException("Test exception") {
          });

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

      doThrow(new DataAccessException("Test exception") {
      })
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
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(productId)
          .userId(UUID.randomUUID())
          .build();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(4)
          .comment("Good product")
          .productUserId(productUserId)
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");
      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      // Configurar para encontrar productUserEntities
      when(jpaProductUserRepository.findByProductId(productId))
          .thenReturn(List.of(productUserEntity));

      // Configurar para encontrar reviews
      when(jpaReviewRepository.findByProductUserId(productUserId))
          .thenReturn(List.of(reviewEntity));

      // Configurar el mapper
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

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
      final UUID productUserId = UUID.randomUUID();

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(productId)
          .userId(UUID.randomUUID())
          .build();

      // Configurar para encontrar productUserEntities
      when(jpaProductUserRepository.findByProductId(productId))
          .thenReturn(List.of(productUserEntity));

      // Configurar para encontrar NO reviews
      when(jpaReviewRepository.findByProductUserId(productUserId))
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

      final EntityId productUserEntityId = new EntityId(productUserId);
      final EntityId reviewEntityId = new EntityId(reviewId);

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(productId)
          .userId(userId)
          .build();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(3)
          .comment("Average product")
          .productUserId(productUserId)
          .build();

      final ReviewDetails reviewDetails = ReviewDetails.create(5, "Great product");
      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      // Configurar para encontrar productUserEntities
      when(jpaProductUserRepository.findByUserId(userId))
          .thenReturn(List.of(productUserEntity));

      // Configurar para encontrar reviews
      when(jpaReviewRepository.findByProductUserId(productUserId))
          .thenReturn(List.of(reviewEntity));

      // Configurar el mapper
      when(reviewMapper.toDomain(reviewEntity)).thenReturn(review);

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
      final UUID productUserId = UUID.randomUUID();

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(UUID.randomUUID())
          .userId(userId)
          .build();

      // Configurar para encontrar productUserEntities
      when(jpaProductUserRepository.findByUserId(userId))
          .thenReturn(List.of(productUserEntity));

      // Configurar para encontrar NO reviews
      when(jpaReviewRepository.findByProductUserId(productUserId))
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
    void when_reviewExistsForUser_then_returnTrue() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(3)
          .comment("Average product")
          .productUserId(productUserId)
          .build();

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(UUID.randomUUID())
          .userId(userId)
          .build();

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
      when(jpaProductUserRepository.findById(productUserId)).thenReturn(Optional.of(productUserEntity));

      // Act
      boolean result = target.existsByReviewIdAndUserId(new EntityId(reviewId), new EntityId(userId));

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void when_reviewDoesNotExist_then_returnFalse() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      // Act
      boolean result = target.existsByReviewIdAndUserId(new EntityId(reviewId), new EntityId(userId));

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    void when_productUserRelationDoesNotBelongToUser_then_returnFalse() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID anotherUserId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .rating(3)
          .comment("Average product")
          .productUserId(productUserId)
          .build();

      final ProductUserEntity productUserEntity = ProductUserEntity.builder()
          .productUserId(productUserId)
          .productId(UUID.randomUUID())
          .userId(anotherUserId)  // Diferente usuario al solicitado
          .build();

      when(jpaReviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
      when(jpaProductUserRepository.findById(productUserId)).thenReturn(Optional.of(productUserEntity));

      // Act
      boolean result = target.existsByReviewIdAndUserId(new EntityId(reviewId), new EntityId(userId));

      // Assert
      assertThat(result).isFalse();
    }
  }
}