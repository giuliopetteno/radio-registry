package com.gp.radioregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RadioRegistryApplication {

	static void main(String[] args) {
		SpringApplication.run(RadioRegistryApplication.class, args);

		System.out.println("RadioRegistry app started...");
	}

}
