package com.protean.dsep.bpp.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.protean.beckn.api.model.init.InitRequest;
import com.protean.beckn.api.model.status.StatusRequest;
import com.protean.dsep.bpp.builder.ResponseBuilder;
import com.protean.dsep.bpp.exception.InvalidUserException;
import com.protean.dsep.bpp.service.AuditService;
import com.protean.dsep.bpp.service.SearchService;
import com.protean.dsep.bpp.util.JsonUtil;
import com.protean.dsep.bpp.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

import static com.protean.dsep.bpp.constant.ApplicationConstant.EXTERNAL_CONTEXT_ROOT;

@RestController
@Slf4j
@RequestMapping(EXTERNAL_CONTEXT_ROOT)
public class StatusController {

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private SearchService service;

	@Autowired
	private AuditService auditService;
	
	@Autowired
	private ResponseBuilder responseBuilder;

	@Value("${beckn.req.auth}")
	private boolean isAuthReq;
	
	@Autowired
	SecurityUtil securityUtil;
	
	@PostMapping("/status")
	public ResponseEntity<String> status(@RequestBody String body, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
		log.info("The body in application status request - {}", body);
		
		log.info("isAuthReq ==> {}",isAuthReq);
		StatusRequest model = this.jsonUtil.toModelSnakeCase(body, StatusRequest.class);
		//StatusRequest model = body;
		
		boolean isValidHeader = true;
		String requestBody = body;
		
		if(isAuthReq) {
			try {
				isValidHeader = securityUtil.authorizeHeader(httpHeaders, requestBody);
			} catch (InvalidUserException e) {
				log.error("Auth header verification failed:",e.getMessage());
				return this.responseBuilder.buildNACKResponseEntity(model.getContext(), e.getMessage());
			}
		}
		
		if(isValidHeader) {
			log.info("Authentication Successful!");
			
			this.auditService.saveAudit(model.getContext(), requestBody);
			
			CompletableFuture.runAsync(() -> {
				try {
					this.service.send(model);
				} catch (Exception e) {
					log.error("error while sending on_status reply", e);
				}
			});
		}

		return this.responseBuilder.buildResponseEntity(model.getContext());
	}

}
