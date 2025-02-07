package com.api.myvanitys.domain.model;

import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.Data;
import java.util.Objects;

@Data
public class Review {
    private final EntityId id;
    private final User user;
    private final Product product;
    private final int rating;
    private final String description;

    public Review(EntityId id, User user, Product product, int rating, String review) {
        this.id = id;
        this.user = user;
        this.product = product;
        this.rating = rating;
        this.description = review;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Review review = (Review) object;

        return rating == review.rating &&
                Objects.equals(id, review.id) &&
                Objects.equals(user, review.user) &&
                Objects.equals(product, review.product) &&
                Objects.equals(this.description, review.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, product, rating, description);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", user=" + user +
                ", product=" + product +
                ", rating=" + rating +
                ", review='" + description + '\'' +
                '}';
    }
}
