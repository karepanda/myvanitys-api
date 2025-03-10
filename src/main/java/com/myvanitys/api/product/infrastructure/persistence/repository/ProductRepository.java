package com.myvanitys.api.product.infrastructure.persistence.repository;

import java.util.UUID;

import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

}
