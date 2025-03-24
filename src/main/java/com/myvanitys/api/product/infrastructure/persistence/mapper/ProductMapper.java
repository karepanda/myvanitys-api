package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Category;
import com.myvanitys.api.product.domain.Product;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface ProductMapper {

  @Mapping(source = "productId", target = "id")
  @Mapping(source = "category.categoryId", target = "category.categoryId")
  Product toDomain(ProductEntity entity);

  @Mapping(source = "id", target = "productId")
  @Mapping(source = "category.categoryId", target = "category.categoryId")
  @Mapping(target = "category", source = "category")
  ProductEntity toEntity(Product domain);

  // Método para mapear Category a CategoryEntity
  default CategoryEntity mapCategoryToCategoryEntity(Category category) {
    if (category == null || category.categoryId() == null) {
      return null;
    }
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(category.categoryId().getValue());
    return categoryEntity;
  }
}