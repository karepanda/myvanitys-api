package com.myvanitys.api.product.domain;

import java.util.Objects;

import com.myvanitys.api.auth.domain.model.User;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
@Data
public class Review {
    private final EntityId id;
    private final User user;
    private final Product product;
    private final Integer rating;
    private final String description;

    public Review(EntityId id, @NonNull User user, @NonNull Product product, @NonNull Integer rating, @NonNull String review) {

        this.id = id;
        this.user = user;
        this.product = product;
        this.rating = rating;
        this.description = review;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, product, rating, description);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Review review = (Review) object;
        return Objects.equals(id, review.id) && Objects.equals(user, review.user) && Objects.equals(product, review.product) && Objects.equals(rating, review.rating) && Objects.equals(description, review.description);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", user=" + user +
                ", product=" + product +
                ", rating=" + rating +
                ", description='" + description + '\'' +
                '}';
    }

}
