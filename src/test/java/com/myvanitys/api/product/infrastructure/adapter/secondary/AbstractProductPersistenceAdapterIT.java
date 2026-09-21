package com.myvanitys.api.product.infrastructure.adapter.secondary;

import java.util.List;
import java.util.Set;

import com.myvanitys.api.common.AbstractIntegrationTest;
import com.myvanitys.api.common.valueobject.EntityId;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.model.ProductUserRelation;
import com.myvanitys.api.product.domain.model.Review;
import com.myvanitys.api.product.domain.valueobject.ReviewDetails;
import com.myvanitys.api.product.infrastructure.persistence.entity.CategoryEntity;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaCategoryRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter-level integration-test base that wires the real production adapter graph against a real PostgreSQL 16
 * Testcontainer. Flyway owns the schema and Hibernate only validates it.
 *
 * <p>Every test runs inside a rolled-back transaction so Flyway seed data is never mutated and tests stay isolated.</p>
 *
 * <p>{@code @DirtiesContext(BEFORE_CLASS)} is required because the inherited {@code @Container} is restarted for every
 * test class while Spring would otherwise reuse a cached context still pointing at the previous container port. It is
 * the only safe option because {@code AbstractIntegrationTest} must not be modified.</p>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
    })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
abstract class AbstractProductPersistenceAdapterIT extends AbstractIntegrationTest {

  @Autowired
  protected CategoryRepositoryAdapter categoryRepositoryAdapter;

  @Autowired
  protected ProductUserRepositoryAdapter productUserRepositoryAdapter;

  @Autowired
  protected ReviewRepositoryAdapter reviewRepositoryAdapter;

  @Autowired
  protected ProductRepositoryAdapter productRepositoryAdapter;

  // JPA repositories and the EntityManager are verification/flush probes only; the operations under test always go
  // through the adapters above.
  @Autowired
  protected JpaCategoryRepository jpaCategoryRepository;

  @Autowired
  protected JpaProductRepository jpaProductRepository;

  @Autowired
  protected JpaProductUserRepository jpaProductUserRepository;

  @Autowired
  protected JpaReviewRepository jpaReviewRepository;

  @PersistenceContext
  protected EntityManager entityManager;

  /**
   * Name of a category inserted by Flyway, used as a deterministic prerequisite for product persistence.
   */
  protected static final String SEEDED_CATEGORY_NAME = "Face";

  protected void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  protected Category seededCategory() {
    return categoryRepositoryAdapter.findByName(SEEDED_CATEGORY_NAME)
        .orElseThrow(() -> new IllegalStateException("Flyway seed category not found: " + SEEDED_CATEGORY_NAME));
  }

  protected Category persistCategory(String name) {
    return categoryRepositoryAdapter.save(new Category(EntityId.newId(), name));
  }

  protected CategoryEntity insertCategoryDirectly(String name) {
    CategoryEntity entity = new CategoryEntity();
    entity.setName(name);
    return jpaCategoryRepository.saveAndFlush(entity);
  }

  protected Product persistProduct(Category category, String name, String brand, String colorHex) {
    Product product = Product.reconstruct(
        EntityId.newId(), name, brand, category, colorHex, List.of(), Set.of());
    return productRepositoryAdapter.save(product);
  }

  protected ProductUserRelation persistRelation(EntityId productId, EntityId userId) {
    productUserRepositoryAdapter.saveProductUserRelationship(productId, userId);
    return productUserRepositoryAdapter
        .findByProductIdAndUserId(productId.getValue(), userId.getValue())
        .orElseThrow(() -> new IllegalStateException("Relation was not persisted"));
  }

  protected Review persistReview(EntityId productUserId, int rating, String comment) {
    Review review = Review.createWithExistingId(
        EntityId.newId(), productUserId, ReviewDetails.create(rating, comment));
    return reviewRepositoryAdapter.save(review);
  }
}
