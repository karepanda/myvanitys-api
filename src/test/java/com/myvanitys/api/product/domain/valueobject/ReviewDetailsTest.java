package com.myvanitys.api.product.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewDetailsTest {

  private final int rating = 4;

  private final String comment = "Test Comment";

  private final Instant now = Instant.now();

  private final Instant yesterday = now.minusSeconds(86400);

  private final Instant tomorrow = now.plusSeconds(86400);

  private ReviewDetails target;

  @BeforeEach
  void setUp() {
    target = ReviewDetails.create(rating, comment);
  }

  @Nested
  class Create {

    @Test
    void when_validParameters_then_returnsNewInstance() {
      // When
      final ReviewDetails result = ReviewDetails.create(5, "New Comment");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.rating()).isEqualTo(5);
      assertThat(result.comment()).isEqualTo("New Comment");
      assertThat(result.createdAt()).isNotNull();
      assertThat(result.updatedAt()).isNotNull();
      assertThat(result.createdAt()).isEqualTo(result.updatedAt());
      assertThat(result.deletedAt()).isNull();
    }

    @Test
    void when_invalidRating_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> ReviewDetails.create(0, comment))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void when_emptyComment_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> ReviewDetails.create(rating, ""))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Comment cannot be empty");
    }
  }

  @Nested
  class Of {

    @Test
    void when_validParameters_then_returnsNewInstance() {
      // When
      final ReviewDetails result = ReviewDetails.of(5, "New Comment", yesterday, now, tomorrow);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.rating()).isEqualTo(5);
      assertThat(result.comment()).isEqualTo("New Comment");
      assertThat(result.createdAt().asInstant()).isEqualTo(yesterday);
      assertThat(result.updatedAt().asInstant()).isEqualTo(now);
      assertThat(result.deletedAt().asInstant()).isEqualTo(tomorrow);
    }

    @Test
    void when_nullTimestamps_then_usesCurrentTimeForCreatedAndUpdated() {
      // When
      final ReviewDetails result = ReviewDetails.of(rating, comment, null, null, null);

      // Then
      assertThat(result.createdAt()).isNotNull();
      assertThat(result.updatedAt()).isNotNull();
      assertThat(result.deletedAt()).isNull();
    }

    @Test
    void when_invalidRating_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> ReviewDetails.of(0, comment, null, null, null))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void when_emptyComment_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> ReviewDetails.of(rating, "", null, null, null))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Comment cannot be empty");
    }
  }

  @Nested
  class WithUpdates {

    @Test
    void when_validParameters_then_returnsNewInstanceWithUpdatedValues() {
      // Given
      final int newRating = 5;
      final String newComment = "Updated Comment";

      // When
      final ReviewDetails result = target.withUpdates(newRating, newComment);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.rating()).isEqualTo(newRating);
      assertThat(result.comment()).isEqualTo(newComment);
      assertThat(result.createdAt()).isEqualTo(target.createdAt());
      assertThat(result.updatedAt()).isNotNull();
      // Updated timestamp should be more recent
      assertThat(result.updatedAt().asInstant()).isAfterOrEqualTo(target.createdAt().asInstant());
      assertThat(result.deletedAt()).isNull();
    }

    @Test
    void when_invalidRating_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> target.withUpdates(0, comment))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void when_emptyComment_then_throwsValidationException() {
      // When/Then
      assertThatThrownBy(() -> target.withUpdates(rating, ""))
          .isInstanceOf(ReviewValidationException.class)
          .hasMessageContaining("Comment cannot be empty");
    }
  }

  @Nested
  class MarkAsDeleted {

    @Test
    void when_notDeleted_then_returnsNewInstanceWithDeletedTimestamp() {
      // When
      final ReviewDetails result = target.markAsDeleted();

      // Then
      assertThat(result).isNotNull();
      assertThat(result.rating()).isEqualTo(target.rating());
      assertThat(result.comment()).isEqualTo(target.comment());
      assertThat(result.createdAt()).isEqualTo(target.createdAt());
      assertThat(result.updatedAt()).isEqualTo(target.updatedAt());
      assertThat(result.deletedAt()).isNotNull();
    }

    @Test
    void when_alreadyDeleted_then_returnsOriginalInstance() {
      // Given
      final ReviewDetails deletedDetails = target.markAsDeleted();

      // When
      final ReviewDetails result = deletedDetails.markAsDeleted();

      // Then
      assertThat(result).isEqualTo(deletedDetails);
      assertThat(result.deletedAt()).isEqualTo(deletedDetails.deletedAt());
    }
  }

  @Nested
  class IsDeleted {

    @Test
    void when_notDeleted_then_returnsFalse() {
      // When/Then
      assertThat(target.isDeleted()).isFalse();
    }

    @Test
    void when_deleted_then_returnsTrue() {
      // Given
      final ReviewDetails deletedDetails = target.markAsDeleted();

      // When/Then
      assertThat(deletedDetails.isDeleted()).isTrue();
    }
  }

  @Nested
  class EqualsAndHashCode {

    @Test
    void when_sameValues_then_equalsReturnsTrue() {
      // Given
      final Timestamp createdAt = Timestamp.of(yesterday);
      final Timestamp updatedAt = Timestamp.of(now);
      final ReviewDetails details1 = new ReviewDetails(rating, comment, createdAt, updatedAt, null);
      final ReviewDetails details2 = new ReviewDetails(rating, comment, createdAt, updatedAt, null);

      // When/Then
      assertThat(details1).isEqualTo(details2);
      assertThat(details1.hashCode()).isEqualTo(details2.hashCode());
    }

    @Test
    void when_differentValues_then_equalsReturnsFalse() {
      // Given
      final ReviewDetails differentRating = ReviewDetails.create(5, comment);
      final ReviewDetails differentComment = ReviewDetails.create(rating, "Different Comment");

      // When/Then
      assertThat(target).isNotEqualTo(differentRating);
      assertThat(target).isNotEqualTo(differentComment);
    }

    @Test
    void when_comparedToNull_then_equalsReturnsFalse() {
      // When/Then
      assertThat(target).isNotEqualTo(null);
    }

    @Test
    void when_comparedToDifferentClass_then_equalsReturnsFalse() {
      // When/Then
      assertThat(target).isNotEqualTo("This is a string, not ReviewDetails");
    }

    @Test
    void when_comparedToSameObject_then_equalsReturnsTrue() {
      // When/Then
      assertThat(target).isEqualTo(target);
    }
  }
}