package com.myvanitys.infrastructure.adapters.outbound.persistence.repository;

import java.util.UUID;

import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

}
