package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface CategoryMapper {
  @Mapping(source = "categoryId", target = "id")
  @Mapping(source = "name", target = "name")
  Category toDomain(CategoryEntity entity);

  @Mapping(source = "id", target = "categoryId")
  @Mapping(source = "name", target = "name")
  CategoryEntity toEntity(Category domain);

}
