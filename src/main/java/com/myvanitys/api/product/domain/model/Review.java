package com.myvanitys.api.product.domain.model;

import java.util.Objects;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Review {

  private final EntityId id;

  private final EntityId userId;

  private final Product product;

  private final Integer rating;

  private final String comment;

  public Review(EntityId id,
      @NonNull EntityId userId,
      @NonNull Product product,
      @NonNull Integer rating,
      @NonNull String comment) {
    this.id = id;
    this.userId = userId;
    this.product = product;
    this.rating = rating;
    this.comment = comment;
  }

  /**
   * Método de fábrica para crear una reseña a partir de una relación de producto-usuario
   *
   * @param id El ID de la reseña
   * @param productUserRelation La relación de producto-usuario existente
   * @param rating La calificación
   * @param comment El comentario
   * @param product El producto asociado a la reseña
   * @return Una nueva instancia de Review
   */
  public static Review createFromRelation(
      EntityId id,
      @NonNull ProductUserRelation productUserRelation,
      @NonNull Integer rating,
      @NonNull String comment,
      @NonNull Product product) {

    return new Review(
        id,
        productUserRelation.getUserId(),
        product,
        rating,
        comment
    );
  }

  /**
   * Método de fábrica para crear una reseña nueva
   *
   * @param userId El ID del usuario
   * @param product El producto
   * @param rating La calificación
   * @param comment El comentario
   * @return Una nueva instancia de Review
   */
  public static Review create(
      @NonNull EntityId userId,
      @NonNull Product product,
      @NonNull Integer rating,
      @NonNull String comment) {

    return new Review(
        new EntityId(), // Genera un nuevo ID
        userId,
        product,
        rating,
        comment
    );
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    Review review = (Review) object;
    return Objects.equals(id, review.id) &&
        Objects.equals(userId, review.userId) &&
        Objects.equals(product, review.product) &&
        Objects.equals(rating, review.rating) &&
        Objects.equals(comment, review.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, product, rating, comment);
  }

  @Override
  public String toString() {
    return "Review{" +
        "id=" + id +
        ", userId=" + userId +
        ", product=" + product +
        ", rating=" + rating +
        ", comment='" + comment + '\'' +
        '}';
  }
}