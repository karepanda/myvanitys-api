package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.myvanitys.api.common.ValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.domain.valueobject.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewTest {

  private EntityId id;

  private EntityId productUserId;

  private ReviewDetails details;

  private Review target;

  private final Instant now = Instant.now();

  private final Instant yesterday = now.minusSeconds(86400);

  private final Instant tomorrow = now.plusSeconds(86400);

  @BeforeEach
  void setUp() {
    id = new EntityId(UUID.randomUUID());
    productUserId = new EntityId(UUID.randomUUID());
    details = ReviewDetails.create(4, "Test Comment");
    target = Review.createWithExistingId(id, productUserId, details);
  }

  @Nested
  class CreateFor {

    @Test
    void when_givenProductUserIdAndDetails_then_returnsNewReview() {
      // Given
      final EntityId newProductUserId = new EntityId(UUID.randomUUID());
      final ReviewDetails newDetails = ReviewDetails.create(5, "New Comment");

      // When
      final Review result = Review.createFor(newProductUserId, newDetails);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isNotNull();
      assertThat(result.getProductUserId()).isEqualTo(newProductUserId);
      assertThat(result.getRating()).isEqualTo(5);
      assertThat(result.getComment()).isEqualTo("New Comment");
    }

    @Test
    void when_givenProductUserIdAndRatingAndComment_then_returnsNewReview() {
      // Given
      final EntityId newProductUserId = new EntityId(UUID.randomUUID());
      final int rating = 3;
      final String comment = "Simple Comment";

      // When
      final Review result = Review.createFor(newProductUserId, rating, comment);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isNotNull();
      assertThat(result.getProductUserId()).isEqualTo(newProductUserId);
      assertThat(result.getRating()).isEqualTo(rating);
      assertThat(result.getComment()).isEqualTo(comment);
    }

    @Test
    void when_givenProductUserIdAndRatingAndCommentAndTimestamps_then_returnsNewReview() {
      // Given
      final EntityId newProductUserId = new EntityId(UUID.randomUUID());
      final int rating = 2;
      final String comment = "Timestamped Comment";

      // When
      final Review result = Review.createFor(newProductUserId, rating, comment, yesterday, now);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isNotNull();
      assertThat(result.getProductUserId()).isEqualTo(newProductUserId);
      assertThat(result.getRating()).isEqualTo(rating);
      assertThat(result.getComment()).isEqualTo(comment);
      assertThat(result.getCreatedAt()).isEqualTo(yesterday);
      assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void when_givenNullProductUserId_then_throwsNullPointerException() {
      // When/Then
      assertThatThrownBy(() -> Review.createFor(null, details))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void when_givenNullComment_then_throwsNullPointerException() {
      // When/Then
      assertThatThrownBy(() -> Review.createFor(productUserId, 4, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void when_givenInvalidRating_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> Review.createFor(productUserId, 0, "Valid Comment"))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");

      assertThatThrownBy(() -> Review.createFor(productUserId, 6, "Valid Comment"))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void when_givenEmptyComment_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> Review.createFor(productUserId, 4, ""))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Comment cannot be empty");

      assertThatThrownBy(() -> Review.createFor(productUserId, 4, "   "))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Comment cannot be empty");
    }
  }

  @Nested
  class CreateWithExistingId {

    @Test
    void when_givenIdAndProductUserIdAndDetails_then_returnsReviewWithSpecificId() {
      // Given
      final EntityId existingId = new EntityId(UUID.randomUUID());
      final EntityId existingProductUserId = new EntityId(UUID.randomUUID());
      final ReviewDetails existingDetails = ReviewDetails.create(5, "Existing Comment");

      // When
      final Review result = Review.createWithExistingId(existingId, existingProductUserId, existingDetails);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(existingId);
      assertThat(result.getProductUserId()).isEqualTo(existingProductUserId);
      assertThat(result.getRating()).isEqualTo(5);
      assertThat(result.getComment()).isEqualTo("Existing Comment");
    }

    @Test
    void when_givenIdAndProductUserIdAndRatingAndCommentAndTimestamps_then_returnsReviewWithSpecificId() {
      // Given
      final EntityId existingId = new EntityId(UUID.randomUUID());
      final EntityId existingProductUserId = new EntityId(UUID.randomUUID());
      final int rating = 1;
      final String comment = "Complete Comment";

      // When
      final Review result = Review.createWithExistingId(existingId, existingProductUserId,
          rating, comment, yesterday, now, tomorrow);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(existingId);
      assertThat(result.getProductUserId()).isEqualTo(existingProductUserId);
      assertThat(result.getRating()).isEqualTo(rating);
      assertThat(result.getComment()).isEqualTo(comment);
      assertThat(result.getCreatedAt()).isEqualTo(yesterday);
      assertThat(result.getUpdatedAt()).isEqualTo(now);
      assertThat(result.getDeletedAt()).isEqualTo(tomorrow);
    }

    @Test
    void when_givenNullProductUserId_then_throwsNullPointerException() {
      // When/Then
      assertThatThrownBy(() -> Review.createWithExistingId(id, null, details))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class UpdateDetails {

    @Test
    void when_givenRatingAndComment_then_updatesDetailsWithNewTimestamp() {
      // Given
      final int newRating = 5;
      final String newComment = "Updated Comment";

      // When
      target.updateDetails(newRating, newComment);

      // Then
      assertThat(target.getRating()).isEqualTo(newRating);
      assertThat(target.getComment()).isEqualTo(newComment);
      // Updated timestamp should be more recent than created timestamp
      assertThat(target.getUpdatedAt()).isAfterOrEqualTo(target.getCreatedAt());
    }

    @Test
    void when_givenCompleteDetails_then_replacesDetailsCompletely() {
      // Given
      final ReviewDetails newDetails = ReviewDetails.of(2, "Completely New", now, tomorrow, null);

      // When
      target.updateDetails(newDetails);

      // Then
      assertThat(target.getRating()).isEqualTo(2);
      assertThat(target.getComment()).isEqualTo("Completely New");
      assertThat(target.getCreatedAt()).isEqualTo(now);
      assertThat(target.getUpdatedAt()).isEqualTo(tomorrow);
    }

    @Test
    void when_givenInvalidRating_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> target.updateDetails(0, "Valid Comment"))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");

      // Ensure original values weren't changed
      assertThat(target.getRating()).isEqualTo(4);
      assertThat(target.getComment()).isEqualTo("Test Comment");
    }

    @Test
    void when_givenEmptyComment_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> target.updateDetails(4, ""))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Comment cannot be empty");

      // Ensure original values weren't changed
      assertThat(target.getRating()).isEqualTo(4);
      assertThat(target.getComment()).isEqualTo("Test Comment");
    }

    @Test
    void when_givenNullComment_then_throwsNullPointerException() {
      // When/Then
      assertThatThrownBy(() -> target.updateDetails(4, null))
          .isInstanceOf(NullPointerException.class);

      // Ensure original values weren't changed
      assertThat(target.getRating()).isEqualTo(4);
      assertThat(target.getComment()).isEqualTo("Test Comment");
    }
  }

  @Nested
  class MarkAsDeleted {

    @Test
    void when_called_then_marksReviewAsDeletedWithCurrentTime() {
      // Given
      assertThat(target.isDeleted()).isFalse();

      // When
      target.markAsDeleted();

      // Then
      assertThat(target.isDeleted()).isTrue();
      assertThat(target.getDeletedAt()).isNotNull();
    }

    @Test
    void when_givenSpecificTimestamp_then_marksReviewAsDeletedWithThatTimestamp() {
      // Given
      assertThat(target.isDeleted()).isFalse();

      // When
      target.markAsDeleted(tomorrow);

      // Then
      assertThat(target.isDeleted()).isTrue();
      assertThat(target.getDeletedAt()).isEqualTo(tomorrow);
    }

    @Test
    void when_alreadyDeleted_then_doesNothing() {
      // Given
      target.markAsDeleted(yesterday);
      assertThat(target.isDeleted()).isTrue();
      assertThat(target.getDeletedAt()).isEqualTo(yesterday);

      // When
      target.markAsDeleted(tomorrow);

      // Then - still has original deletion timestamp
      assertThat(target.getDeletedAt()).isEqualTo(yesterday);
    }

    @Test
    void when_givenNullTimestamp_then_doesNothing() {
      // Given
      assertThat(target.isDeleted()).isFalse();

      // When
      target.markAsDeleted(null);

      // Then
      assertThat(target.isDeleted()).isFalse();
      assertThat(target.getDeletedAt()).isNull();
    }
  }

  @Nested
  class IsDeleted {

    @Test
    void when_reviewIsNotDeleted_then_returnsFalse() {
      // When/Then
      assertThat(target.isDeleted()).isFalse();
    }

    @Test
    void when_reviewIsDeleted_then_returnsTrue() {
      // Given
      target.markAsDeleted();

      // When/Then
      assertThat(target.isDeleted()).isTrue();
    }
  }

  @Nested
  class GetRating {

    @Test
    void when_called_then_returnsRatingFromDetails() {
      // Given
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.rating()).thenReturn(5);
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final int result = reviewWithMock.getRating();

      // Then
      assertThat(result).isEqualTo(5);
    }
  }

  @Nested
  class GetComment {

    @Test
    void when_called_then_returnsCommentFromDetails() {
      // Given
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.comment()).thenReturn("Mocked Comment");
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final String result = reviewWithMock.getComment();

      // Then
      assertThat(result).isEqualTo("Mocked Comment");
    }
  }

  @Nested
  class GetCreatedAt {

    @Test
    void when_called_then_returnsCreatedAtTimestampAsInstant() {
      // Given
      final Timestamp mockTimestamp = mock(Timestamp.class);
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.createdAt()).thenReturn(mockTimestamp);
      when(mockTimestamp.asInstant()).thenReturn(yesterday);
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final Instant result = reviewWithMock.getCreatedAt();

      // Then
      assertThat(result).isEqualTo(yesterday);
    }
  }

  @Nested
  class GetUpdatedAt {

    @Test
    void when_called_then_returnsUpdatedAtTimestampAsInstant() {
      // Given
      final Timestamp mockTimestamp = mock(Timestamp.class);
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.updatedAt()).thenReturn(mockTimestamp);
      when(mockTimestamp.asInstant()).thenReturn(now);
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final Instant result = reviewWithMock.getUpdatedAt();

      // Then
      assertThat(result).isEqualTo(now);
    }
  }

  @Nested
  class GetDeletedAt {

    @Test
    void when_reviewIsNotDeleted_then_returnsNull() {
      // Given
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.deletedAt()).thenReturn(null);
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final Instant result = reviewWithMock.getDeletedAt();

      // Then
      assertThat(result).isNull();
    }

    @Test
    void when_reviewIsDeleted_then_returnsDeletedAtTimestampAsInstant() {
      // Given
      final Timestamp mockTimestamp = mock(Timestamp.class);
      final ReviewDetails mockDetails = mock(ReviewDetails.class);
      when(mockDetails.deletedAt()).thenReturn(mockTimestamp);
      when(mockTimestamp.asInstant()).thenReturn(tomorrow);
      final Review reviewWithMock = Review.createWithExistingId(id, productUserId, mockDetails);

      // When
      final Instant result = reviewWithMock.getDeletedAt();

      // Then
      assertThat(result).isEqualTo(tomorrow);
    }
  }

  @Nested
  class EqualsAndHashCode {

    @Test
    void when_sameId_then_equalsReturnsTrue() {
      // Given
      final Review otherReview = Review.createWithExistingId(
          id,
          new EntityId(UUID.randomUUID()),
          ReviewDetails.create(1, "Different Comment")
      );

      // When/Then
      assertThat(target).isEqualTo(otherReview);
      assertThat(target.hashCode()).isEqualTo(otherReview.hashCode());
    }

    @Test
    void when_differentId_then_equalsReturnsFalse() {
      // Given
      final Review otherReview = Review.createWithExistingId(
          new EntityId(UUID.randomUUID()),
          productUserId,
          details
      );

      // When/Then
      assertThat(target).isNotEqualTo(otherReview);
    }

    @Test
    void when_comparedToNull_then_equalsReturnsFalse() {
      // When/Then
      assertThat(target).isNotEqualTo(null);
    }

    @Test
    void when_comparedToDifferentClass_then_equalsReturnsFalse() {
      // When/Then
      assertThat(target).isNotEqualTo("This is a string, not a Review");
    }

    @Test
    void when_comparedToSameObject_then_equalsReturnsTrue() {
      // When/Then
      assertThat(target).isEqualTo(target);
    }
  }
}