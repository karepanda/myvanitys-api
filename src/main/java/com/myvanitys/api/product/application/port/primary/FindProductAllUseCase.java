package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.domain.model.Product;

import java.util.List;


public interface FindProductAllUseCase {

  /**
   * Retrieves all available products
   *
   * @return a list of all products
   */
  List<Product> query();
}