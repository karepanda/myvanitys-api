package com.myvanitys.api.product.infrastructure.adapter.primary.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.myvanitys.api.model.v1.CategoryResponse;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.model.v1.ReviewResponse;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductResponseMapperTest {

  private ProductResponseMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ProductResponseMapper.class);
  }

  @Nested
  class ProductToResponse {

    @Test
    void when_validProduct_then_convertToResponseCorrectly() {
      // Arrange
      Category category = new Category(EntityId.newId(), "Category Name");

      Product product = Product.newProduct("Product Name", "Brand Name", "#FFFFFF");
      product.assignCategory(category);

      // Act
      ProductResponse response = mapper.toResponse(product);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(product.getId().getValue());
      assertThat(response.getName()).isEqualTo(product.getName());
      assertThat(response.getBrand()).isEqualTo(product.getBrand());
      assertThat(response.getCategory()).isNotNull();
      assertThat(response.getColorHex()).isEqualTo(product.getColorHex());
    }

    @Test
    void when_productWithReviews_then_mapReviewsCorrectly() {
      // Arrange
      EntityId userId = EntityId.newId();
      EntityId productUserId = EntityId.newId(); // ID de la relación producto-usuario

      Category category = new Category(EntityId.newId(), "Category Name");

      // Crear review manualmente
      ReviewDetails reviewDetails = ReviewDetails.of(5, "Excellent product!",
          Instant.now(), Instant.now(), null);
      Review review = Review.createWithExistingId(EntityId.newId(), productUserId, reviewDetails);

      // Crear ProductUserRelation
      ProductUserRelation userRelation = ProductUserRelation.create(
          EntityId.newId(), // productId será reemplazado por reconstruct
          userId);

      // Reconstruct product con reviews y relaciones
      Product product = Product.reconstruct(
          EntityId.newId(),
          "Product Name",
          "Brand Name",
          category,
          "#FFFFFF",
          List.of(review),
          Set.of(userRelation)
      );

      // Act
      ProductResponse response = mapper.toResponse(product);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getReviews()).isNotNull();
      assertThat(response.getReviews()).hasSize(1);
      assertThat(response.getReviews().getFirst().getRating()).isEqualTo(5);
      assertThat(response.getReviews().getFirst().getComment()).isEqualTo("Excellent product!");
    }
  }

  @Nested
  class ProductListToResponseList {

    @Test
    void when_validProductList_then_convertToResponseListCorrectly() {
      // Arrange
      Product product1 = Product.newProduct("Product 1", "Description 1", "#FF0000");
      Product product2 = Product.newProduct("Product 2", "Description 2", "#00FF00");
      List<Product> products = Arrays.asList(product1, product2);

      // Act
      List<ProductResponse> responses = mapper.toResponseList(products);

      // Assert
      assertThat(responses).hasSize(2);
      assertThat(responses.get(0).getName()).isEqualTo("Product 1");
      assertThat(responses.get(1).getName()).isEqualTo("Product 2");
    }
  }

  @Nested
  class CategoryToResponse {

    @Test
    void when_validCategory_then_convertToCategoryResponseCorrectly() {
      // Arrange
      EntityId categoryId = EntityId.newId();
      Category category = new Category(categoryId, "Test Category");

      // Act
      CategoryResponse response = mapper.toCategoryResponse(category);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(categoryId.getValue());
      assertThat(response.getName()).isEqualTo("Test Category");
    }
  }

  @Nested
  class ReviewToResponse {

    @Test
    void when_validReview_then_convertToReviewResponseCorrectly() {
      // Arrange
      EntityId productUserId = EntityId.newId();
      Instant now = Instant.now();
      ReviewDetails details = ReviewDetails.of(5, "Great product!", now, now, null);
      Review review = Review.createWithExistingId(EntityId.newId(), productUserId, details);

      // Act
      ReviewResponse response = mapper.toReviewResponse(review);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(review.getId().getValue());
      assertThat(response.getRating()).isEqualTo(5);
      assertThat(response.getComment()).isEqualTo("Great product!");

    }

    @Test
    void when_nullReview_then_returnNull() {
      // Act
      ReviewResponse response = mapper.toReviewResponse(null);

      // Assert
      assertThat(response).isNull();
    }
  }

  @Nested
  class ReviewListToResponseList {

    @Test
    void when_validReviewList_then_convertToResponseListCorrectly() {
      // Arrange
      EntityId productUserId1 = EntityId.newId();
      EntityId productUserId2 = EntityId.newId();
      Instant now = Instant.now();

      ReviewDetails details1 = ReviewDetails.of(5, "Excellent!", now, null, null);
      ReviewDetails details2 = ReviewDetails.of(4, "Very good", now, null, null);

      Review review1 = Review.createWithExistingId(EntityId.newId(), productUserId1, details1);
      Review review2 = Review.createWithExistingId(EntityId.newId(), productUserId2, details2);

      List<Review> reviews = Arrays.asList(review1, review2);

      // Act
      List<ReviewResponse> responses = mapper.toReviewResponseList(reviews);

      // Assert
      assertThat(responses).hasSize(2);
      assertThat(responses.get(0).getRating()).isEqualTo(5);
      assertThat(responses.get(0).getComment()).isEqualTo("Excellent!");
      assertThat(responses.get(1).getRating()).isEqualTo(4);
      assertThat(responses.get(1).getComment()).isEqualTo("Very good");
    }

    @Test
    void when_emptyReviewList_then_returnEmptyList() {
      // Arrange
      List<Review> reviews = List.of();

      // Act
      List<ReviewResponse> responses = mapper.toReviewResponseList(reviews);

      // Assert
      assertThat(responses).isEmpty();
    }

    @Test
    void when_nullReviewList_then_returnNull() {
      // Act
      List<ReviewResponse> responses = mapper.toReviewResponseList(null);

      // Assert
      assertThat(responses).isNull();
    }
  }

  @Nested
  class HelperMethods {

    @Test
    void when_validEntityId_then_convertToUuidCorrectly() {
      // Arrange
      EntityId entityId = EntityId.newId();

      // Act
      var uuid = mapper.entityIdToUuid(entityId);

      // Assert
      assertThat(uuid).isEqualTo(entityId.getValue());
    }

    @Test
    void when_nullEntityId_then_returnNull() {
      // Act
      var uuid = mapper.entityIdToUuid(null);

      // Assert
      assertThat(uuid).isNull();
    }

    @Test
    void when_positiveInt_then_convertToFloat() {
      // Act
      Float result = mapper.intToValidatedFloat(5);

      // Assert
      assertThat(result).isEqualTo(5.0f);
    }

    @Test
    void when_zeroOrNegativeInt_then_returnNull() {
      // Act
      Float resultZero = mapper.intToValidatedFloat(0);
      Float resultNegative = mapper.intToValidatedFloat(-1);

      // Assert
      assertThat(resultZero).isNull();
      assertThat(resultNegative).isNull();
    }
  }
}