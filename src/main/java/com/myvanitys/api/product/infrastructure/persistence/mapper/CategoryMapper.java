package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.exception.CategoryValidationException;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.CategoryCode;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface CategoryMapper {

  @Mapping(target = "code", source = "code", qualifiedByName = "categoryCodeToString")
  CategoryEntity toEntity(Category domain);

  @Mapping(target = "code", source = "code", qualifiedByName = "stringToCategoryCode")
  Category toDomain(CategoryEntity entity);

  @Named("stringToCategoryCode")
  default CategoryCode stringToCategoryCode(String code) {
    return CategoryCode.fromValue(code);
  }

  @Named("categoryCodeToString")
  default String categoryCodeToString(CategoryCode code) {
    if (code == null) {
      throw new CategoryValidationException("Category code cannot be null");
    }
    return code.getValue();
  }
}
