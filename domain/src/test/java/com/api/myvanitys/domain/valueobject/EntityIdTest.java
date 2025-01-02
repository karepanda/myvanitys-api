package com.api.myvanitys.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityIdTest {

  private EntityId target;

  @Nested
  class Constructor {

    @Test
    void when_defaultConstructor_then_generateNewUUID() {
      // Given & When
      target = new EntityId();

      // Then
      assertThat(target.getValue()).isNotNull();
    }

    @Test
    void when_uuidProvided_then_valueIsSet() {
      // Given
      final UUID uuid = UUID.randomUUID();

      // When
      target = new EntityId(uuid);

      // Then
      assertThat(target.getValue()).isEqualTo(uuid);
    }

    @Test
    void when_nullUuidProvided_then_throwException() {

      assertThatThrownBy(() -> new EntityId(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Id value cannot be null");
    }
  }

  @Nested
  class GetValue {

    @Test
    void when_called_then_returnValue() {
      // Given
      final UUID uuid = UUID.randomUUID();
      target = new EntityId(uuid);

      // When
      final UUID result = target.getValue();

      // Then
      assertThat(result).isEqualTo(uuid);
    }
  }

  @Nested
  class Equals {

    @Test
    void when_equalValues_then_returnTrue() {
      // Given
      final UUID uuid = UUID.randomUUID();
      target = new EntityId(uuid);
      final EntityId other = new EntityId(uuid);

      // When
      final boolean result = target.equals(other);

      // Then
      assertThat(result).isTrue();
    }

    @Test
    void when_differentValues_then_returnFalse() {
      // Given
      target = new EntityId(UUID.randomUUID());
      final EntityId other = new EntityId(UUID.randomUUID());

      // When
      final boolean result = target.equals(other);

      // Then
      assertThat(result).isFalse();
    }
  }

  @Nested
  class HashCode {

    @Test
    void when_called_then_returnConsistentHashCode() {
      // Given
      final UUID uuid = UUID.randomUUID();
      target = new EntityId(uuid);

      // When
      final int firstHashCode = target.hashCode();
      final int secondHashCode = target.hashCode();

      // Then
      assertThat(firstHashCode).isEqualTo(secondHashCode);
    }

    @Nested
    class ToString {

      @Test
      void when_called_then_returnUuidAsString() {
        // Given
        final UUID uuid = UUID.randomUUID();
        target = new EntityId(uuid);

        // When
        final String result = target.toString();

        // Then
        assertThat(result).isEqualTo(uuid.toString());
      }
    }
  }
}