package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.domain.model.Product;

/**
 * Input port for adding a review to a product
 */
public interface AddReviewToProductUseCase {

  /**
   * Adds a review to a product
   *
   * @param command The command containing the review details
   * @return The updated product with the new review
   */
  Product execute(AddReviewToProductCommand command);
}
