package com.myvanitys.api.product.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class EntityIdMapperTest {

  private EntityIdMapper entityIdMapper;

  private UUID testUuid;

  private EntityId testEntityId;

  @BeforeEach
  void setUp() {
    // Obtener la instancia de implementación usando Mappers.getMapper
    entityIdMapper = Mappers.getMapper(EntityIdMapper.class);

    // Crear datos de prueba
    testUuid = UUID.randomUUID();
    testEntityId = new EntityId(testUuid);
  }

  @Test
  void toEntityId_WhenGivenValidUuid_ShouldReturnEntityId() {
    // Act
    EntityId result = entityIdMapper.toEntityId(testUuid);

    // Assert
    assertEquals(testUuid, result.getValue());
  }

  @Test
  void toEntityId_WhenGivenNull_ShouldReturnNull() {
    // Act
    EntityId result = entityIdMapper.toEntityId(null);

    // Assert
    assertNull(result);
  }

  @Test
  void toUUID_WhenGivenValidEntityId_ShouldReturnUuid() {
    // Act
    UUID result = entityIdMapper.toUUID(testEntityId);

    // Assert
    assertEquals(testUuid, result);
  }

  @Test
  void toUUID_WhenGivenNull_ShouldReturnNull() {
    // Act
    UUID result = entityIdMapper.toUUID(null);

    // Assert
    assertNull(result);
  }

  @Test
  void roundTrip_FromUuidToEntityIdAndBack_ShouldReturnOriginalUuid() {
    // Act
    EntityId entityId = entityIdMapper.toEntityId(testUuid);
    UUID result = entityIdMapper.toUUID(entityId);

    // Assert
    assertEquals(testUuid, result);
  }

  @Test
  void roundTrip_FromEntityIdToUuidAndBack_ShouldReturnOriginalEntityId() {
    // Act
    UUID uuid = entityIdMapper.toUUID(testEntityId);
    EntityId result = entityIdMapper.toEntityId(uuid);

    // Assert
    assertEquals(testEntityId, result);
  }
}