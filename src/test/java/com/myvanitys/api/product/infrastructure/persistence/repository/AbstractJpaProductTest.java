package com.myvanitys.api.product.infrastructure.persistence.repository;

import com.myvanitys.api.common.AbstractIntegrationTest;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.test.annotation.FlywayTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@ActiveProfiles("test")
@FlywayTest
@AutoConfigureEmbeddedDatabase
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers
public abstract class AbstractJpaProductTest extends AbstractIntegrationTest {

  @Autowired
  protected JpaProductRepository jpaProductRepository;

  @Autowired
  protected JpaCategoryRepository jpaCategoryRepository;

  @Autowired
  protected JpaProductUserRepository jpaProductUserRepository;

  @Autowired
  protected JpaReviewRepository jpaReviewRepository;
}