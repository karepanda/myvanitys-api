package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

}
