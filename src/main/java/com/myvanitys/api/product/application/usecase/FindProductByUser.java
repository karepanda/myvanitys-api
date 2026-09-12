package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindProductByUser implements FindProductUserUseCase {

  private final ProductRepository productRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Product> query(FindProductUserQuery query) {
    return productRepository.findAllProductDetailsByUserId(query.userId().getValue());

  }
}