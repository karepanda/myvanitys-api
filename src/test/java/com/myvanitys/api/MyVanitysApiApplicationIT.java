package com.myvanitys.api;

import com.myvanitys.api.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.docker.compose.enabled=false",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("test")
class MyVanitysApiApplicationIT extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
  }

}
