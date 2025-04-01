package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.UUID;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityIdMapper {

  default EntityId toEntityId(UUID uuid) {
    return uuid == null ? null : new EntityId(uuid);
  }

  default UUID toUUID(EntityId entityId) {
    return entityId == null ? null : entityId.getValue();
  }
}
