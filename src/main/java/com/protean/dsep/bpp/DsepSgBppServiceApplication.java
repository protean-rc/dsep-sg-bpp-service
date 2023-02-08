package com.protean.dsep.bpp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DsepSgBppServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DsepSgBppServiceApplication.class, args);
	}

}
