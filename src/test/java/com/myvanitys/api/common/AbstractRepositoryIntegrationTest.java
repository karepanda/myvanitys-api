package com.myvanitys.api.common.test;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@FlywayTest
@AutoConfigureEmbeddedDatabase
public abstract class AbstractRepositoryIntegrationTest {
  // Métodos de utilidad comunes o configuración adicional que quieras compartir

  // Por ejemplo, podrías tener helpers para crear entidades de test
  // o para verificar condiciones comunes en tus tests
}