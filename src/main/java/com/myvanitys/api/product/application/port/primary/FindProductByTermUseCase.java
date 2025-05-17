package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.domain.model.Product;

import java.util.List;

public interface FindProductByTermUseCase {

  /**
   * Lists all products associated with a specific user
   *
   * @param term object containing search parameters
   * @return a list of products associated with the user
   */
  List<Product> query(String term);

}
