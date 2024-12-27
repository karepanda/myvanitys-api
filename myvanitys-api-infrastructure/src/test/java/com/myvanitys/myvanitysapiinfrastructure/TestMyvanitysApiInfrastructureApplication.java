package com.myvanitys.myvanitysapiinfrastructure;

import org.springframework.boot.SpringApplication;

public class TestMyvanitysApiInfrastructureApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyvanitysApiInfrastructureApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
