package com.myvanitys.adapters;

import com.myvanitys.infraestructure.adapters.Application;
import org.springframework.boot.SpringApplication;

public class TestMyvanitysApiInfrastructureApplication {

	public static void main(String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
