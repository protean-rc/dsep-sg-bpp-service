package com.protean.dsep.bpp.util;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.protean.dsep.bpp.exception.ApplicationException;
import com.protean.dsep.bpp.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JsonUtil {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	@Qualifier("snakeCaseObjectMapper")
	private ObjectMapper mapperSnakeCase;

	public String toJson(Object request) {
		try {
			return this.mapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			log.error("error while building json" + e);
			throw new ApplicationException(ErrorCode.JSON_PROCESSING_ERROR);
		}
	}

	public String toJsonSnakeCase(Object request) {
		try {
			return this.mapperSnakeCase.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			log.error("error while building json" + e);
			throw new ApplicationException(ErrorCode.JSON_PROCESSING_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass toModel(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapper.readValue(body, schemaClass);
			log.debug("The {} model is {}", schemaClass, model);
			return model;
		} catch (JsonProcessingException e) {
			log.error("Error while json processing for {}. Not a valid json {}", schemaClass, e);
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass toModelSnakeCase(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapperSnakeCase.readValue(body, schemaClass);
			log.debug("The {} model is {}", schemaClass, model);
			return model;
		} catch (JsonProcessingException e) {
			log.error("Error while json processing for {}. Not a valid json {}", schemaClass, e);
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	public <schemaClass> schemaClass toModelList(String body, Class<?> schemaClass) {
		try {
			schemaClass model = (schemaClass) this.mapper.readValue(body, this.mapper.getTypeFactory().constructCollectionType(List.class, schemaClass));
			log.debug("The {} model list is {}", schemaClass, model);
			return model;
		} catch (JsonProcessingException e) {
			log.error("Error while json processing for {}. Not a valid json {}", schemaClass, e);
			throw new ApplicationException(ErrorCode.INVALID_REQUEST);
		}
	}

	public String unpretty(String json) {
		try {
			String[] lines = json.split("\n");
			return Stream.of(lines)
					.map(String::trim)
					.reduce(String::concat)
					.orElseThrow();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
