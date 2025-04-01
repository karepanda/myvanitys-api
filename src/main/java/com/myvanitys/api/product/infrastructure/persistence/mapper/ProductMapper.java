package com.myvanitys.api.product.infrastructure.persistence.mapper;

import java.util.List;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public abstract class ProductMapper {

  @Autowired
  protected JpaCategoryRepository jpaCategoryRepository;

  @Mapping(target = "id", source = "productId")
  @Mapping(target = "category", source = "category", qualifiedByName = "categoryEntityToCategory")
  public abstract Product toDomain(ProductEntity productEntity);

  @Mapping(target = "productId", source = "id")
  @Mapping(target = "category", source = "category", qualifiedByName = "categoryToCategoryEntity")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract ProductEntity toEntity(Product product);

  public abstract List<Product> toDomainList(List<ProductEntity> productEntities);

  public abstract List<ProductEntity> toEntityList(List<Product> products);

  @Named("categoryEntityToCategory")
  protected Category categoryEntityToCategory(CategoryEntity categoryEntity) {
    if (categoryEntity == null || categoryEntity.getCategoryId() == null) {
      return null;
    }

    return new Category(
        new EntityId(categoryEntity.getCategoryId()),
        categoryEntity.getName()
    );
  }

  @Named("categoryToCategoryEntity")
  protected CategoryEntity categoryToCategoryEntity(Category category) {
    if (category == null || category.categoryId() == null) {
      return null;
    }

    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setCategoryId(category.categoryId().getValue());
    categoryEntity.setName(category.name());

    return categoryEntity;
  }
}