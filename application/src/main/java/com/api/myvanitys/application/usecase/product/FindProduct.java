package com.api.myvanitys.application.usecase.product;

import java.util.Optional;

import com.api.myvanitys.application.query.FindProductQuery;
import com.api.myvanitys.domain.model.Product;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import com.myvanitys.infrastructure.adapters.outbound.persistence.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindProduct {

  private final ProductRepository productRepository; // Inject repository

  private final ProductMapper productMapper;  // Inject mapper

  public Product execute(FindProductQuery query) {
    Optional<ProductEntity> productEntity = productRepository.findByName(query.getSearchTerms());

    if (productEntity.isEmpty()) {
      productEntity = productRepository.findByBrand(query.getSearchTerms());
    }

    return productEntity.map(productMapper::toProduct) // Mapper ProductEntity -> Product (Domain ob)
        .orElse(null); // return null if product not found*/
  }
}
