package com.myvanitys.api.product.application.port.primary;

import java.util.List;

import com.myvanitys.api.product.application.query.FindProductUserQuery;
import com.myvanitys.api.product.domain.model.Product;

public interface FindProductUserUseCase {

  /**
   * Lists all products associated with a specific user
   *
   * @param query object containing search parameters
   * @return a list of products associated with the user
   */
  List<Product> query(FindProductUserQuery query);

}
