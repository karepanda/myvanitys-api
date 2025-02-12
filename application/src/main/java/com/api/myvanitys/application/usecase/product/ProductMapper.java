package com.api.myvanitys.application.usecase.product;

import com.api.myvanitys.domain.model.Category;
import com.api.myvanitys.domain.model.Product;
import com.api.myvanitys.domain.valueobject.EntityId;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.CategoryEntity;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "mapUUIDToEntityId")
    @Mapping(source = "categoryEntity", target = "category")
    @Mapping(source = "colorHex", target = "colorHex", qualifiedByName = "mapColorToHex")
    Product toDomain(ProductEntity entity);

    @Mapping(source = "id", target = "id", qualifiedByName = "mapEntityIdToUUID")
    @Mapping(source = "category", target = "categoryEntity")
    @Mapping(source = "colorHex", target = "colorHex", qualifiedByName = "mapHexToColor")
    ProductEntity toEntity(Product product);

    default Product toProduct(ProductEntity entity) {
        return toDomain(entity);
    }

    @Named("mapUUIDToEntityId")
    static EntityId mapUUIDToEntityId(UUID id) {
        return id != null ? new EntityId(id) : null;
    }

    @Named("mapEntityIdToUUID")
    static UUID mapEntityIdToUUID(EntityId entityId) {
        return entityId != null ? entityId.getValue() : null;
    }

    @Named("mapColorToHex")
    static String mapColorToHex(Integer color) {
        return (color != null) ? String.format("#%06X", (0xFFFFFF & color)) : null;
    }

    @Named("mapHexToColor")
    static Integer mapHexToColor(String colorHex) {
        return (colorHex != null && colorHex.startsWith("#")) ? Integer.parseInt(colorHex.substring(1), 16) : null;
    }

    // Mapeo entre CategoryEntity y Category
    @Named("mapCategoryEntityToCategory")
    static Category mapCategoryEntityToCategory(CategoryEntity categoryEntity) {
        return categoryEntity != null ? new Category(new EntityId(categoryEntity.getId()), categoryEntity.getName()) : null;
    }

    // Mapeo entre Category y CategoryEntity
    @Named("mapCategoryToCategoryEntity")
    static CategoryEntity mapCategoryToCategoryEntity(Category category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(category.getId().getValue());
        categoryEntity.setName(category.getName());
        return categoryEntity;
    }
}
