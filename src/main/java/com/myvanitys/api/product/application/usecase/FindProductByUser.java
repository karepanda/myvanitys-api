package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.entity.ProductEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindProductByUser implements FindProductUserUseCase {

  private final JpaProductRepository jpaProductRepository;

  private final FindProductService findProductService;

  @Override
  @Transactional(readOnly = true)
  public List<Product> query(FindProductUserQuery query) {
    List<ProductEntity> productEntities = jpaProductRepository.findByUserId(query.userId().getValue());

    return findProductService.findProducts(productEntities);

  }
}