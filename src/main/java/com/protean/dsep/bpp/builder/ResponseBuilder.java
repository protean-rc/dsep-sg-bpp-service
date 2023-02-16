package com.protean.dsep.bpp.builder;

import java.time.LocalDateTime;
import java.util.Date;

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
import com.protean.beckn.api.model.common.Country;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Location;
import com.protean.beckn.api.model.response.Response;
import com.protean.beckn.api.model.response.ResponseMessage;
import com.protean.dsep.bpp.exception.ErrorCode;
import com.protean.dsep.bpp.util.CommonUtil;

@Service
public class ResponseBuilder {

	@Autowired
	@Qualifier("snakeCaseObjectMapper")
	private ObjectMapper snakeCaseMapper;

	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Autowired
	CommonUtil commonUtil;
	
	public Context buildContext(Context context, String action) {
		context.setAction(action);
		context.setBppUri(this.sellerUrl);
		return context;
	}

	public ResponseEntity<String> buildResponseEntity(Context ctx) throws JsonProcessingException {
		Location location = new Location();
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

		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
		
		response.setContext(context);
		return this.snakeCaseMapper.writeValueAsString(response);
	}

	public ResponseEntity<String> buildNACKResponseEntity(Context ctx, String error) throws JsonProcessingException {
		Location location = new Location();
		City city = new City();
		city.setName("Bangalore");
		city.setCode("std:080");
		location.setCity(city);
		
		Country country = new Country();
		country.setName("India");
		country.setCode("IND");
		location.setCountry(country);
		
		ctx.setLocation(location);
		
		String response = buildNACKResponse(ctx, error);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.headers(headers)
				.body(response);
	}
	
	private String buildNACKResponse(Context context, String error) throws JsonProcessingException {
		Response response = new Response();
		ResponseMessage resMsg = new ResponseMessage();

		resMsg.setAck(new Ack(AckStatus.NACK));
		response.setMessage(resMsg);
		
		com.protean.beckn.api.model.common.Error err = new Error();
		err.setCode(error);
		err.setMessage(ErrorCode.CODE_MSG.get(error));
		response.setError(err);
		
		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
		
		response.setContext(context);
		return this.snakeCaseMapper.writeValueAsString(response);
	}
}
