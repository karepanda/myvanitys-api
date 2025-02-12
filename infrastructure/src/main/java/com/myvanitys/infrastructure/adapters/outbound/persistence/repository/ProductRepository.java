package com.myvanitys.infrastructure.adapters.outbound.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findByName(String name);
    Optional<ProductEntity> findByBrand(String brand);
}
