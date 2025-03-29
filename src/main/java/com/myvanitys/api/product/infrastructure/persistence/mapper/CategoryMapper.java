package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface CategoryMapper {

  Category toDomain(CategoryEntity entity);

  CategoryEntity toEntity(Category domain);
}
