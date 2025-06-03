package com.myvanitys.api.product.infrastructure.adapter.primary.mapper;

import com.myvanitys.api.model.v1.CategoryResponse;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.model.v1.ReviewResponse;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;


@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

  /**
   * Converts a domain Product object to ProductResponse for the API
   *
   * @param product domain object
   * @return API response object
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "category", target = "category")
  @Mapping(source = "reviews", target = "reviews")
  @Mapping(target = "averageRating", expression = "java(product.getAverageRating() > 0 ? (float)product.getAverageRating() : null)")
  @Mapping(target = "createdAt", ignore = true)
  ProductResponse toResponse(Product product);

  /**
   * Converts a list of domain Products to a list of ProductResponse for the API
   *
   * @param products list of domain objects
   * @return list of API response objects
   */
  List<ProductResponse> toResponseList(List<Product> products);

  /**
   * Converts a domain Category object (record) to CategoryResponse for the API
   *
   * @param category domain object (record)
   * @return API response object
   */
  @Mapping(source = "categoryId.value", target = "id")
  @Mapping(source = "name", target = "name")
  CategoryResponse toCategoryResponse(Category category);

  /**
   * Converts a domain Review object to ReviewResponse for the API
   *
   * @param review domain Review object
   * @return API response object
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "productUserId.value", target = "userId")
  @Mapping(source = "rating", target = "rating")
  @Mapping(source = "comment", target = "comment")
  @Mapping(target = "createdAt", ignore = true)
  ReviewResponse toReviewResponse(Review review);

  /**
   * Converts a list of domain Reviews to a list of ReviewResponse for the API
   *
   * @param reviews list of domain objects
   * @return list of API response objects
   */
  List<ReviewResponse> toReviewResponseList(List<Review> reviews);

  /**
   * Method to handle conversions from EntityId to UUID
   */
  @Named("entityIdToUuid")
  default UUID entityIdToUuid(EntityId id) {
    return id != null ? id.getValue() : null;
  }

  /**
   * Method to handle conversions from int to Float with validation
   */
  @Named("intToValidatedFloat")
  default Float intToValidatedFloat(int value) {
    return value > 0 ? (float) value : null;
  }
}