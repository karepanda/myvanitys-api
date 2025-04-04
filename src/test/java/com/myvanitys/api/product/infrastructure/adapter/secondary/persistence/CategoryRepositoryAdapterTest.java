package com.myvanitys.api.product.infrastructure.adapter.secondary.persistence;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {
    @InjectMocks
    private CategoryRepositoryAdapter target;

    @Mock
    private JpaCategoryRepository jpaCategoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private EntityId categoryId;

    private Category category;

    private CategoryEntity categoryEntity;

    @BeforeEach
    void setUp() {
        // Initialize IDs
        categoryId = new EntityId(UUID.randomUUID());

        // Initialize domain objects
        category = new Category(categoryId, "Test Category");

        // Initialize entity objects
        categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryId.getValue());
        categoryEntity.setName("Test Category");
    }

    @Nested
    class save {
        @Test
        void shouldSaveCategory() {
            // Given
            when(categoryMapper.toEntity(category)).thenReturn(categoryEntity);
            when(jpaCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
            when(categoryMapper.toDomain(categoryEntity)).thenReturn(category);

            // When
            Category savedCategory = target.save(category);

            // Then
            assertThat(savedCategory).isEqualTo(category);
            verify(jpaCategoryRepository).save(categoryEntity);
        }
    }


}