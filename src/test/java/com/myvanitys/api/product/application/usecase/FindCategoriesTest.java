package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.CategoryCode;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindCategoriesTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindCategories target;

    @Nested
    class Query {

        @Test
        void when_repositoryReturnsCategories_then_returnsListOfCategories() {
            // Arrange
            final Category face = new Category(new EntityId(UUID.randomUUID()), "Face", CategoryCode.FACE);
            final Category eyes = new Category(new EntityId(UUID.randomUUID()), "Eyes", CategoryCode.EYES);
            final List<Category> expectedCategories = List.of(face, eyes);

            when(categoryRepository.findAll()).thenReturn(expectedCategories);

            // Act
            final List<Category> result = target.query();

            // Assert
            assertThat(result).isEqualTo(expectedCategories);
        }

        @Test
        void when_repositoryReturnsEmptyList_then_returnsEmptyCategoryList() {
            // Arrange
            final List<Category> expectedCategories = List.of();

            when(categoryRepository.findAll()).thenReturn(expectedCategories);

            // Act
            final List<Category> result = target.query();

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
