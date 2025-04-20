package com.myvanitys.api.product.domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.myvanitys.api.product.domain.exception.ProductValidationException;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Product {

  private final EntityId id;

  private String name;

  private String brand;

  private Category category;

  private String colorHex;

  private int averageRating;

  private final List<Review> reviews = new ArrayList<>();

  private final Set<ProductUserRelation> userRelations = new HashSet<>();

  public Product(EntityId id, String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
  }

  public void updateDetails(String name, String brand, Category category, String colorHex) {
    validateProductDetails(name, brand, colorHex);
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
  }

  private void validateProductDetails(String name, String brand, String colorHex) {
    validateNotEmpty(name, "name");
    validateNotEmpty(brand, "brand");
    validateColorHex(colorHex);
  }

  private void validateNotEmpty(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new ProductValidationException(fieldName + " cannot be empty");
    }
  }

  private void validateColorHex(String colorHex) {
    if (colorHex != null && !colorHex.trim().isEmpty() &&
        !colorHex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
      throw new ProductValidationException("Invalid color hex format");
    }
  }

  public int getAverageRating() {
    return reviews.isEmpty() ? 0 : reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
  }

  public void addReview(Review review) {
    if (review == null) {
      throw new ProductValidationException("Review cannot be null");
    }
    if (!reviews.contains(review)) {
      reviews.add(review);
      calculateAverageRating();
    }
  }

  public void removeReview(Review review) {
    if (reviews.remove(review)) {
      calculateAverageRating();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  private void calculateAverageRating() {
    averageRating = reviews.isEmpty() ? 0 :
        reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
  }
}
