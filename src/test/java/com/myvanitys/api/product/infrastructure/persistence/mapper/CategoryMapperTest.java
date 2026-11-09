package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

  @InjectMocks
  private final CategoryMapper target = Mappers.getMapper(CategoryMapper.class);

  @Mock
  private EntityIdMapper entityIdMapper;

  @Nested
  class ToDomain {

    @Test
    void when_givenCategoryEntity_then_returnCategory() {
      // Given
      final UUID categoryId = UUID.randomUUID();
      final String name = "Skincare";
      final String code = "SERUM";
      final CategoryEntity entity = new CategoryEntity();
      entity.setCategoryId(categoryId);
      entity.setName(name);
      entity.setCode(code);
      final EntityId entityId = new EntityId(categoryId);

      when(entityIdMapper.toEntityId(categoryId)).thenReturn(entityId);

      // When
      final Category result = target.toDomain(entity);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.categoryId()).isEqualTo(entityId);
      assertThat(result.name()).isEqualTo(name);
      assertThat(result.code()).isEqualTo(code);
    }
  }

  @Nested
  class ToEntity {

    @Test
    void when_givenCategory_then_returnCategoryEntity() {
      // Given
      final UUID categoryId = UUID.randomUUID();
      final String name = "Makeup";
      final String code = "LIPS";
      final EntityId entityId = new EntityId(categoryId);
      final Category domain = new Category(entityId, name, code);
      final CategoryEntity expectedEntity = new CategoryEntity();
      expectedEntity.setCategoryId(categoryId);
      expectedEntity.setName(name);
      expectedEntity.setCode(code);

      when(entityIdMapper.toUUID(entityId)).thenReturn(categoryId);

      // When
      final CategoryEntity result = target.toEntity(domain);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getCategoryId()).isEqualTo(expectedEntity.getCategoryId());
      assertThat(result.getName()).isEqualTo(expectedEntity.getName());
      assertThat(result.getCode()).isEqualTo(expectedEntity.getCode());
    }
  }

}