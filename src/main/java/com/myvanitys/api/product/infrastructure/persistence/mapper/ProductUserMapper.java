package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductUserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ProductUserMapper {

  public ProductUserEntity createProductUserEntity(EntityId userId, EntityId productId) {
    ProductUserEntity entity = new ProductUserEntity();
    entity.setUserId(userId.getValue());
    entity.setProductId(productId.getValue());
    return entity;
  }
}
