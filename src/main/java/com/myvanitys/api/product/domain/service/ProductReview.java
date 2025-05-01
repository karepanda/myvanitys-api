package com.myvanitys.api.product.domain.service;

import java.util.Optional;

import com.myvanitys.api.product.domain.exception.ReviewValidationException;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewService {

  public Review addReview(Product product, EntityId userId, ReviewDetails reviewDetails) {
    if (product.hasReviewFrom(userId)) {
      throw new ReviewValidationException("User already reviewed this product");
    }

    Review review = new Review(reviewDetails.content(), reviewDetails.rating());
    product.addReview(review, userId);
    return review;
  }

  public void updateReview(Product product, EntityId userId, ReviewDetails reviewDetails) {
    if (!product.hasReviewFrom(userId)) {
      throw new ReviewValidationException("No review found to update for this user");
    }

    product.updateReview(userId, reviewDetails.content(), reviewDetails.rating());
  }

  public void deleteReview(Product product, EntityId userId) {
    if (!product.hasReviewFrom(userId)) {
      throw new ReviewValidationException("No review found to delete for this user");
    }

    product.deleteReview(userId);
  }

  public Optional<Review> getReview(Product product, EntityId userId) {
    return product.getReviewFrom(userId);
  }
}
