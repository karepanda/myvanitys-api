package com.myvanitys.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myvanitys.api.product.domain.exception.CategoryValidationException;
import org.junit.jupiter.api.Test;

class CategoryCodeTest {

  @Test
  void exposesExactlyTheEightSupportedCodes() {
    assertThat(CategoryCode.values())
        .containsExactly(
            CategoryCode.FACE,
            CategoryCode.EYES,
            CategoryCode.LASHES,
            CategoryCode.BROWS,
            CategoryCode.LIPS,
            CategoryCode.CREAM,
            CategoryCode.SERUM,
            CategoryCode.TONER);
  }

  @Test
  void everySupportedCodeResolvesFromItsValue() {
    for (CategoryCode code : CategoryCode.values()) {
      assertThat(CategoryCode.fromValue(code.getValue())).isEqualTo(code);
    }
  }

  @Test
  void nullValueIsRejected() {
    assertThatThrownBy(() -> CategoryCode.fromValue(null))
        .isInstanceOf(CategoryValidationException.class);
  }

  @Test
  void blankValueIsRejected() {
    assertThatThrownBy(() -> CategoryCode.fromValue(""))
        .isInstanceOf(CategoryValidationException.class);
    assertThatThrownBy(() -> CategoryCode.fromValue("   "))
        .isInstanceOf(CategoryValidationException.class);
  }

  @Test
  void unknownValueIsRejected() {
    assertThatThrownBy(() -> CategoryCode.fromValue("UNKNOWN"))
        .isInstanceOf(CategoryValidationException.class);
  }

  @Test
  void incorrectlyCasedValueIsRejected() {
    assertThatThrownBy(() -> CategoryCode.fromValue("face"))
        .isInstanceOf(CategoryValidationException.class);
    assertThatThrownBy(() -> CategoryCode.fromValue("Face"))
        .isInstanceOf(CategoryValidationException.class);
    assertThatThrownBy(() -> CategoryCode.fromValue("eyes"))
        .isInstanceOf(CategoryValidationException.class);
    assertThatThrownBy(() -> CategoryCode.fromValue("Lips"))
        .isInstanceOf(CategoryValidationException.class);
  }
}
