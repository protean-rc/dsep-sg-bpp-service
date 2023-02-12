package com.protean.dsep.bpp.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.protean.beckn.api.enums.AckStatus;
import com.protean.beckn.api.model.common.Ack;
import com.protean.beckn.api.model.common.City;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.ContextLocation;
import com.protean.beckn.api.model.common.Country;
import com.protean.beckn.api.model.response.Response;
import com.protean.beckn.api.model.response.ResponseMessage;

@Service
public class ResponseBuilder {

	@Autowired
	@Qualifier("snakeCaseObjectMapper")
	private ObjectMapper snakeCaseMapper;

	@Value("${beckn.seller.url}")
	private String sellerUrl;

	public Context buildContext(Context context, String action) {
		context.setAction(action);
		context.setBppUri(this.sellerUrl);
		return context;
	}

	public ResponseEntity<String> buildResponseEntity(Context ctx) throws JsonProcessingException {
		ContextLocation location = new ContextLocation();
		City city = new City();
		city.setName("Bangalore");
		city.setCode("std:080");
		location.setCity(city);
		
		Country country = new Country();
		country.setName("India");
		country.setCode("IND");
		location.setCountry(country);
		
		ctx.setLocation(location);
		
		String response = buildAckResponse(ctx);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.headers(headers)
				.body(response);
	}

	private String buildAckResponse(Context context) throws JsonProcessingException {
		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.ACK));
		response.setMessage(resMsg);

		response.setContext(context);
		return this.snakeCaseMapper.writeValueAsString(response);
	}

}
