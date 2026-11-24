package com.myvanitys.api.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public abstract class AbstractIntegrationTest {

  // JUnit owns and closes static fields annotated with @Container.
  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
      .withDatabaseName("myvanitysdb")
      .withUsername("myvanitys")
      .withPassword("secret");

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}
