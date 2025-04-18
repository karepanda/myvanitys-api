package com.myvanitys.api.product.infrastructure.persistence.repository;

import com.myvanitys.api.common.AbstractIntegrationTest;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(SpringRunner.class)
@DataJpaTest
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
}