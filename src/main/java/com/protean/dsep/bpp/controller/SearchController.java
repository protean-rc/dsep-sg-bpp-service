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
import com.protean.beckn.api.model.search.SearchRequest;
import com.protean.dsep.bpp.builder.ResponseBuilder;
import com.protean.dsep.bpp.exception.ErrorCode;
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
public class SearchController {

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private SearchService service;

	@Autowired
	private AuditService auditService;
	
	@Autowired
	private ResponseBuilder responseBuilder;
	
	@Autowired
	private SecurityUtil securityUtil;

	@Value("${beckn.req.auth}")
	private boolean isAuthReq;
	
	@PostMapping("/search")
	public ResponseEntity<String> search(@RequestBody String body, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
		log.info("The body in search request - {}", body);
		log.info("isAuthReq ==> {}",isAuthReq);
		SearchRequest model = this.jsonUtil.toModelSnakeCase(body, SearchRequest.class);
		//SearchRequest model = body;
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
			try {
				this.auditService.saveAudit(model.getContext(), requestBody);
			} catch (Exception e1) {
				log.error("Error occured while auditing SEARCH request-",e1);
			}
			
			CompletableFuture.runAsync(() -> {
				try {
					this.service.send(model);
				} catch (Exception e) {
					log.error("error while sending on_search reply", e);
				}
			});

		}

		return this.responseBuilder.buildResponseEntity(model.getContext());
	}

}
