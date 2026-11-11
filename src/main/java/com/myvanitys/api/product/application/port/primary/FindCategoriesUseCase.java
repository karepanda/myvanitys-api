package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.domain.model.Category;

import java.util.List;


public interface FindCategoriesUseCase {

  /**
   * Retrieves all available product categories
   *
   * @return a list of all categories
   */
  List<Category> query();
}
