package com.myvanitys.api;

import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GoogleClientProperties.class)
public class MyVanitysApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(MyVanitysApiApplication.class, args);
  }

}
