package com.myvanitys.api.product.application.port.output;

import java.util.List;
import java.util.Optional;

import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.domain.valueobject.EntityId;

public interface ReviewRepositoryPort {

  Review save(Review review);

  Optional<Review> findById(EntityId id);

  List<Review> findByProductId(EntityId productId);

  List<Review> findByUserId(EntityId userId);

}
