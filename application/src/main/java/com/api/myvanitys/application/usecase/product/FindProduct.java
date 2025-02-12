package com.api.myvanitys.application.usecase.product;

import com.api.myvanitys.application.query.FindProductQuery;
import com.api.myvanitys.domain.model.Product;
import com.myvanitys.infrastructure.adapters.outbound.persistence.repository.ProductRepository;

public class FindProduct {

  private final ProductRepository productRepository;

  private final ProductMapper productMapper;  // Agregamos el mapper

  public FindProduct(ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  public Product execute(FindProductQuery query) {
        /*Optional<ProductEntity> productEntity = productRepository.findByName(query.getSearchTerms());

        if (productEntity.isEmpty()) {
            productEntity = productRepository.findByBrand(query.getSearchTerms());
        }

        return productEntity.map(productMapper::toProduct) // Mapeamos ProductEntity -> Product
                .orElse(null); // Retorna null si no encuentra nada*/
    return null;
  }
}
