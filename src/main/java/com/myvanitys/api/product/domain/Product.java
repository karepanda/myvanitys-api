package com.myvanitys.api.product.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

  // Usuarios que tienen este producto en su vanity
  private final Set<ProductUserRelation> userRelations = new HashSet<>();

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
      calculateAverageRating();
    }
  }

  public void removeReview(Review review) {
    if (reviews.remove(review)) {
      calculateAverageRating();
    }
  }

  // Método para asociar un producto a un usuario (añadirlo a su vanity)
  public void addToUserVanity(@NonNull EntityId userId, String reviewText) {
    // Crear la relación producto-usuario
    ProductUserRelation relation = new ProductUserRelation(new EntityId(), this.id, userId);

    // Si se proporciona un review, añadirlo
    if (reviewText != null && !reviewText.trim().isEmpty()) {
      Review review = new Review(new EntityId(), userId, this, 5, reviewText); // Pasar el producto actual (this)
      this.addReview(review);
      relation.setReviewId(review.getId());
    }

    // Añadir la relación a la colección
    userRelations.add(relation);
  }

  // Método para eliminar un producto de la vanity de un usuario
  public void removeFromUserVanity(@NonNull EntityId userId) {
    userRelations.removeIf(relation -> relation.getUserId().equals(userId));
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