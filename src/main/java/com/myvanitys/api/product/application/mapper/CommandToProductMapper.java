package com.myvanitys.api.product.application.mapper;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.mapper.EntityIdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EntityIdMapper.class})
public interface CommandToProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "averageRating", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "userRelations", ignore = true)
  default Product toProduct(CreateProductCommand command, Category category) {
    return Product.create(
        command.name(),
        command.brand(),
        category,
        command.colorHex()
    );
  }
}