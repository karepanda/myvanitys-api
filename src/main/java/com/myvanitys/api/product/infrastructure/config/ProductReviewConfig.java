package com.myvanitys.api.product.infrastructure.config;

import com.myvanitys.api.product.domain.service.ProductReview;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductReviewConfig {

  @Bean
  public ProductReview productReview() {
    return new ProductReview();
  }
}
