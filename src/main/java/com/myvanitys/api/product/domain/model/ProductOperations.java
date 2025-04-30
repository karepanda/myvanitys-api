package com.myvanitys.api.product.domain.model;

/**
 * Sealed interface for operations that can be performed on a Product This enforces that only permitted classes can implement these
 * operations
 */
public sealed interface ProductOperations permits ProductReviewComponent {

  /**
   * Gets the product this component operates on
   */
  Product getProduct();
}