package com.myvanitys.api.product.application.mapper;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;


@Mapper(componentModel = "spring")
public interface CommandToProductMapper {
    @Mapping(target = "id", source = "command.id")
    @Mapping(target = "name", source = "command.name")
    @Mapping(target = "brand", source = "command.brand")
    @Mapping(target = "colorHex", source = "command.colorHex")
    @Mapping(target = "category", source = "category")
    Product toProduct(CreateProductCommand command, Category category);

    default EntityId map(UUID id) {
        return new EntityId(id);
    }
}
