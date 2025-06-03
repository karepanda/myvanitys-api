package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

  private final JpaCategoryRepository jpaCategoryRepository;

  private final CategoryMapper categoryMapper;

  public CategoryRepositoryAdapter(
      JpaCategoryRepository jpaCategoryRepository,
      CategoryMapper categoryMapper) {
    this.jpaCategoryRepository = jpaCategoryRepository;
    this.categoryMapper = categoryMapper;
  }

  @Override
  public Optional<Category> findById(EntityId categoryId) {
    UUID uuid = categoryId.getValue();
    return jpaCategoryRepository.findById(uuid)
        .map(categoryMapper::toDomain);
  }

  @Override
  public Optional<Category> findByName(String name) {
    return jpaCategoryRepository.findByName(name)
        .map(categoryMapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return jpaCategoryRepository.findAll().stream()
        .map(categoryMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public Category save(Category category) {
    CategoryEntity entity = categoryMapper.toEntity(category);
    CategoryEntity savedEntity = jpaCategoryRepository.save(entity);
    return categoryMapper.toDomain(savedEntity);
  }

  @Override
  @Transactional
  public void deleteById(EntityId categoryId) {
    UUID uuid = categoryId.getValue();
    jpaCategoryRepository.deleteById(uuid);
  }
}