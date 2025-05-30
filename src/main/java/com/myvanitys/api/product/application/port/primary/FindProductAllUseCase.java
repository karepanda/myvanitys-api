package com.myvanitys.api.product.application.port.primary;

import com.myvanitys.api.product.domain.model.Product;

import java.util.List;

/**
 * Caso de uso para obtener todos los productos
 */
public interface FindProductAllUseCase {

  /**
   * Recupera todos los productos disponibles
   *
   * @return una lista de todos los productos
   */
  List<Product> query();
}