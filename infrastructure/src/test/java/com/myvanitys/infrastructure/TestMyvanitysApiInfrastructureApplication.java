package com.myvanitys.infrastructure;

import com.myvanitys.infrastructure.adapters.Application;
import org.springframework.boot.SpringApplication;

public class TestMyvanitysApiInfrastructureApplication {

	public static void main(String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
