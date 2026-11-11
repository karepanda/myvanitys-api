package com.myvanitys.api.product.domain.model;

import com.myvanitys.api.product.domain.exception.CategoryValidationException;

/**
 * Stable machine-readable category codes exposed by the API contract.
 *
 * <p>The domain only ever holds one of these eight values. Any other string is rejected at the
 * persistence boundary by {@link #fromValue(String)}, so an unsupported code can never reach the
 * response mapping.</p>
 */
public enum CategoryCode {

  FACE,
  EYES,
  LASHES,
  BROWS,
  LIPS,
  CREAM,
  SERUM,
  TONER;

  /**
   * Resolves a stored code string to its domain value.
   *
   * @param value the raw code value, exactly matching one of the enum names
   * @return the matching {@link CategoryCode}
   * @throws CategoryValidationException if the value is null, blank, unknown, or not exactly cased
   */
  public static CategoryCode fromValue(String value) {
    if (value == null || value.isBlank()) {
      throw new CategoryValidationException("Category code cannot be null or blank");
    }
    for (CategoryCode code : values()) {
      if (code.name().equals(value)) {
        return code;
      }
    }
    throw new CategoryValidationException("Unsupported category code: " + value);
  }

  /**
   * @return the canonical string value persisted and serialized for this code
   */
  public String getValue() {
    return name();
  }
}
