package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class EntityIdMapperTest {

    private final EntityIdMapper target = EntityIdMapper.INSTANCE;

    @Nested
    class ToEntityId {
        @Test
        void when_givenValidUUID_then_returnEntityId() {
            // Given
            final UUID uuid = UUID.randomUUID();

            // When
            final EntityId result = target.toEntityId(uuid);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo(uuid);
        }

        @Test
        void when_givenNullUUID_then_returnNull() {
            // When
            final EntityId result = target.toEntityId(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    class ToUUID {
        @Test
        void when_givenValidEntityId_then_returnUUID() {
            // Given
            final UUID uuid = UUID.randomUUID();
            final EntityId entityId = new EntityId(uuid);

            // When
            final UUID result = target.toUUID(entityId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(uuid);
        }

        @Test
        void when_givenNullEntityId_then_returnNull() {
            // When
            final UUID result = target.toUUID(null);

            // Then
            assertThat(result).isNull();
        }
    }
}