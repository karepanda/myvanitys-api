package com.myvanitys.api.product.application.usecase;

import java.util.List;

import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.mapper.ProductMapper;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProductByUser implements FindProductUserUseCase {

  private final JpaProductRepository jpaProductRepository;

  private final ProductMapper productMapper;

  @Override
  @Transactional(readOnly = true)
  public List<Product> query(FindProductUserQuery query) {
    List<Product> products = jpaProductRepository.findByUserId(query.userId().getValue())
        .stream()
        .map(productMapper::toDomain)
        .toList();

    if (products.isEmpty()) {
      throw ProductNotFoundException.forUser(query.userId());
    }

    return products;
  }
}
