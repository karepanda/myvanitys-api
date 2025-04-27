package com.myvanitys.api.product.application.command;

import java.time.Instant;

import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import lombok.NonNull;

public record AddReviewToProductCommand(@NonNull EntityId userId, @NonNull EntityId productId,
                                        ReviewDetails reviewDetails) {

  public AddReviewToProductCommand(EntityId userId, EntityId productId, int rating, String comment, Instant createdAt) {
    this(userId, productId, ReviewDetails.of(rating, comment, createdAt));
  }

  public AddReviewToProductCommand(EntityId userId, EntityId productId, int rating, String comment) {
    this(userId, productId, ReviewDetails.of(rating, comment));
  }
}