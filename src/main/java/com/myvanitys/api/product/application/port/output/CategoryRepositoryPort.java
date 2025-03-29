package com.myvanitys.api.product.application.port.output;

import java.util.Optional;

import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public interface CategoryRepositoryPort {

  Optional<Category> findById(EntityId id);

}
