package com.myvanitys.api.product.infrastructure.adapter.primary.mapper;

import com.myvanitys.api.model.v1.CategoryResponse;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ProductResponseMapperTest {

    private ProductResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProductResponseMapper.class);
    }

    @Nested
    class ProductToResponse {
        @Test
        void when_validProduct_then_convertToResponseCorrectly(){
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



}