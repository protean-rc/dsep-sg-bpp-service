package com.protean.dsep.bpp.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Configuration
public class AppConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Bean
	@Qualifier("snakeCaseObjectMapper")
	public ObjectMapper snakeCaseObjectMapper() {
		return new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.setSerializationInclusion(Include.NON_NULL)
				.findAndRegisterModules();
	}

}
