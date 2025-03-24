package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EntityIdMapper {

  EntityIdMapper INSTANCE = Mappers.getMapper(EntityIdMapper.class);

  default EntityId toEntityId(UUID uuid) {
    return uuid == null ? null : new EntityId(uuid);
  }

  default UUID toUUID(EntityId entityId) {
    return entityId == null ? null : entityId.getValue();
  }
}
