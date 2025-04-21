package com.myvanitys.api.product.infrastructure.adapter.primary.mapper;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.model.v1.CategoryResponse;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper para convertir entre Product (dominio) y ProductResponse (API). Se utiliza MapStruct para generar automáticamente la
 * implementación en tiempo de compilación.
 */
@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

  /**
   * Convierte un objeto Product de dominio a ProductResponse para la API
   *
   * @param product objeto de dominio
   * @return objeto de respuesta API
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "category", target = "category")
  @Mapping(target = "averageRating", expression = "java(product.getAverageRating() > 0 ? (float)product.getAverageRating() : null)")
  @Mapping(target = "createdAt", ignore = true)
  // Ignorar ya que no está en nuestro modelo de dominio
  ProductResponse toResponse(Product product);

  /**
   * Convierte una lista de Products de dominio a lista de ProductResponse para la API
   *
   * @param products lista de objetos de dominio
   * @return lista de objetos de respuesta API
   */
  List<ProductResponse> toResponseList(List<Product> products);

  /**
   * Convierte un objeto Category de dominio (record) a CategoryResponse para la API
   *
   * @param category objeto de dominio (record)
   * @return objeto de respuesta API
   */
  @Mapping(source = "categoryId.value", target = "id")
  @Mapping(source = "name", target = "name")
  CategoryResponse toCategoryResponse(Category category);

  /**
   * Método para manejar conversiones de EntityId a UUID
   */
  @Named("entityIdToUuid")
  default UUID entityIdToUuid(EntityId id) {
    return id != null ? id.getValue() : null;
  }

  /**
   * Método para manejar conversiones de int a Float con validación
   */
  @Named("intToValidatedFloat")
  default Float intToValidatedFloat(int value) {
    return value > 0 ? (float) value : null;
  }
}