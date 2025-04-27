package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.application.command.AddReviewToProductCommand;
import com.myvanitys.api.product.domain.model.Product;

/**
 * Input port for adding a review to a product
 */
public interface AddReviewToProductUseCase {

  /**
   * Executes the command to add a review to a product.
   *
   * @param command the command containing the details of the review, such as user ID, product ID, rating, comment, and creation
   *     timestamp
   * @return the updated Product with the added review
   */
  Product execute(AddReviewToProductCommand command);
}
