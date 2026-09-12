package com.myvanitys.api.auth.infrastructure.persistence;

import com.myvanitys.api.auth.infrastructure.persistence.repository.JpaUserRepository;
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
public abstract class AbstractJpaAuthTest extends AbstractIntegrationTest {

  @Autowired
  protected JpaUserRepository userRepository;


}