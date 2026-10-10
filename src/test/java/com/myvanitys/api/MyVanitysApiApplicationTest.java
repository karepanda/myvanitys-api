package com.myvanitys.api;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.docker.compose.enabled=false",
    "spring.datasource.url=jdbc:postgresql://localhost/unused",
    "spring.datasource.username=unused",
    "spring.datasource.password=unused",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED)
class MyVanitysApiApplicationTest {

  @Test
  void contextLoads() {
  }

}
