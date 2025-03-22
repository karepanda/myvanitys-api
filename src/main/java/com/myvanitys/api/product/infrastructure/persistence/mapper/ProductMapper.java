package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, EntityIdMapper.class})
public interface ProductMapper {

  @Mapping(source = "productId", target = "id")
  @Mapping(source = "categoryEntity", target = "category")
  @Mapping(target = "averageRating", ignore = true) // Esto se calcula en el dominio
  @Mapping(target = "reviews", ignore = true) // Los reviews normalmente se cargarían por separado
  Product toDomain(ProductEntity entity);

  @Mapping(source = "id", target = "productId")
  @Mapping(source = "category", target = "categoryEntity")
    // No mapeamos averageRating ni reviews a la entidad porque son calculados o cargan por separado
  ProductEntity toEntity(Product domain);
}
