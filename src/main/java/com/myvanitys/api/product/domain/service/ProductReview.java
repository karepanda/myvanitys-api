package com.myvanitys.api.product.domain.service;

import java.util.List;

import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import org.springframework.stereotype.Service;

@Service
public class ProductReview {

  public double calculateOverallAverageRating(List<Product> products) {
    long totalReviews = 0;
    int totalRatingSum = 0;

    for (Product product : products) {
      List<Review> activeReviews = product.getReviews().stream()
          .filter(Review::isActive)
          .toList();

      totalReviews += activeReviews.size();
      totalRatingSum += activeReviews.stream()
          .mapToInt(Review::getRating)
          .sum();
    }

    return totalReviews > 0 ? (double) totalRatingSum / totalReviews : 0;
  }

  private EntityId findUserIdForReview(Product product, EntityId reviewId) {
    return product.getUserRelations().stream()
        .filter(relation -> reviewId.equals(relation.getReviewId()))
        .map(ProductUserRelation::getUserId)
        .findFirst()
        .orElse(null);
  }
}