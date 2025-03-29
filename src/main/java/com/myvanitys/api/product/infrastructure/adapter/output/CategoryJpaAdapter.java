package com.myvanitys.api.product.infrastructure.adapter.output;

import java.util.Optional;

import com.myvanitys.api.product.application.port.output.CategoryRepositoryPort;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.infrastructure.persistence.mapper.CategoryMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CategoryJpaAdapter implements CategoryRepositoryPort {

  private final CategoryRepository repository;

  private final CategoryMapper mapper;

  @Override
  public Optional<Category> findById(EntityId id) {
    return Optional.empty();
  }

  // Constructor e implementación
}