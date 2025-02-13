package com.api.myvanitys.domain.model;

import com.api.myvanitys.domain.valueobject.EntityId;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.util.Objects;
@Data
@ToString
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



}
