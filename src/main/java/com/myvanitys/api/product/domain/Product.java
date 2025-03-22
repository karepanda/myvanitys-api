package com.myvanitys.api.product.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Product {

  private final EntityId id;

  private final String name;

  private final String brand;

  private final Category category;

  private final String colorHex;

  private int averageRating;

  private final List<Review> reviews = new ArrayList<>();

  public Product(EntityId id, @NonNull String name, @NonNull String brand, @NonNull Category category, @NonNull String colorHex) {

    this.id = id;
    this.name = name;
    this.brand = brand;
    this.category = category;
    this.colorHex = colorHex;
    this.averageRating = 0;
  }

  public int getAverageRating() {
    if (reviews.isEmpty()) {
      return 0;
    }

    int totalRating = 0;
    for (Review review : reviews) {
      totalRating += review.getRating();
    }

    return totalRating / reviews.size();
  }

  public void addReview(@NonNull Review review) {
    if (!reviews.contains(review)) {
      reviews.add(review);
    }
  }

  public void removeReview(Review review) {
    if (reviews.remove(review)) {
      calculateAverageRating();
    }
  }

  private void calculateAverageRating() {
    averageRating = reviews.isEmpty() ? 0 :
        reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Product product = (Product) obj;
    return Objects.equals(id, product.id);
  }
}
