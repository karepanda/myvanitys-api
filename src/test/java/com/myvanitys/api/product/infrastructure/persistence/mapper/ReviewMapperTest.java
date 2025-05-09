package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

  @Mock
  private EntityIdMapper entityIdMapper;

  @InjectMocks
  private ReviewMapperImpl target;

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    void when_validEntityAndProductUserId_then_returnsMappedReview() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();
      final EntityId productUserEntityId = new EntityId(productUserId);
      final Instant createdAt = Instant.now().minusSeconds(3600);
      final Instant updatedAt = Instant.now();

      final ReviewEntity reviewEntity = ReviewEntity.builder()
          .reviewId(reviewId)
          .productUserId(productUserId)
          .rating(5)
          .comment("Excelente producto")
          .createdAt(createdAt)
          .updatedAt(updatedAt)
          .build();

      // Act
      final Review result = target.toDomain(reviewEntity);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId().getValue()).isEqualTo(reviewId);
      assertThat(result.getProductUserId()).isEqualTo(productUserEntityId);
      assertThat(result.getDetails().rating()).isEqualTo(5);
      assertThat(result.getDetails().comment()).isEqualTo("Excelente producto");
      assertThat(result.getDetails().createdAt().asInstant()).isEqualTo(createdAt);
      assertThat(result.getDetails().updatedAt().asInstant()).isEqualTo(updatedAt);
    }

    @Test
    void when_entityIsNull_then_returnsNull() {

      // Act
      final Review result = target.toDomain(null);

      // Assert
      assertThat(result).isNull();
    }

  }

  @Nested
  @DisplayName("toEntity")
  class ToEntity {

    @Test
    void when_validReview_then_returnsMappedEntity() {
      // Arrange
      final UUID reviewId = UUID.randomUUID();
      final UUID productUserId = UUID.randomUUID();
      final EntityId reviewEntityId = new EntityId(reviewId);
      final EntityId productUserEntityId = new EntityId(productUserId);

      final Instant createdAt = Instant.now().minusSeconds(3600);
      final Instant updatedAt = Instant.now();

      final ReviewDetails reviewDetails = ReviewDetails.of(
          4,
          "Good product",
          createdAt,
          updatedAt,
          null);

      final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);

      // Configure specific mapping for these IDs
      when(entityIdMapper.toUUID(reviewEntityId)).thenReturn(reviewId);
      when(entityIdMapper.toUUID(productUserEntityId)).thenReturn(productUserId);

      // Act
      final ReviewEntity result = target.toEntity(review);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getReviewId()).isEqualTo(reviewId);
      assertThat(result.getProductUserId()).isEqualTo(productUserId);
      assertThat(result.getRating()).isEqualTo(4);
      assertThat(result.getComment()).isEqualTo("Good product");
      assertThat(result.getCreatedAt()).isEqualTo(createdAt);
      assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
      assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void when_reviewIsNull_then_returnsNull() {
      // Act
      final ReviewEntity result = target.toEntity(null);

      // Assert
      assertThat(result).isNull();
    }

    @Nested
    @DisplayName("ForProductUserId")
    class ForProductUserId {

      @Test
      void when_validReviewAndProductUserId_then_returnsMappedEntityWithOverriddenProductUserId() {
        // Arrange
        final UUID reviewId = UUID.randomUUID();
        final UUID originalProductUserId = UUID.randomUUID();
        final UUID newProductUserId = UUID.randomUUID();
        final EntityId reviewEntityId = new EntityId(reviewId);
        final EntityId productUserEntityId = new EntityId(originalProductUserId);

        final Instant createdAt = Instant.now().minusSeconds(3600);
        final Instant updatedAt = Instant.now();

        final ReviewDetails reviewDetails = ReviewDetails.of(
            4,
            "Good product",
            createdAt,
            updatedAt,
            null);

        final Review review = Review.createWithExistingId(reviewEntityId, productUserEntityId, reviewDetails);
        // Configure default behavior for entityIdMapper
        lenient().when(entityIdMapper.toUUID(any(EntityId.class))).thenAnswer(
            invocation -> {
              EntityId entityId = invocation.getArgument(0);
              return entityId != null ? entityId.getValue() : null;
            });

        lenient().when(entityIdMapper.toEntityId(any(UUID.class))).thenAnswer(
            invocation -> {
              UUID uuid = invocation.getArgument(0);
              return uuid != null ? new EntityId(uuid) : null;
            });

        // Act
        final ReviewEntity result = target.toEntity(review, newProductUserId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getProductUserId()).isEqualTo(newProductUserId);
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Good product");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getDeletedAt()).isNull();
      }

      @Test
      void when_reviewIsNullButProductUserIdProvided_then_throwsNullPointerException() {
        // Arrange
        final UUID productUserId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> target.toEntity(null, productUserId))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining(
                "Cannot invoke \"com.myvanitys.api.product.domain.model.Review.getCreatedAt()\" because \"review\" is null");
      }
    }
  }
}