package com.protean.dsep.bpp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.model.confirm.ConfirmRequest;
import com.protean.beckn.api.model.init.InitRequest;
import com.protean.beckn.api.model.onconfirm.OnConfirmRequest;
import com.protean.beckn.api.model.oninit.OnInitRequest;
import com.protean.beckn.api.model.onsearch.OnSearchRequest;
import com.protean.beckn.api.model.onstatus.OnStatusRequest;
import com.protean.beckn.api.model.search.SearchRequest;
import com.protean.beckn.api.model.status.StatusRequest;
import com.protean.dsep.bpp.builder.OnConfirmBuilder;
import com.protean.dsep.bpp.builder.OnInitBuilder;
import com.protean.dsep.bpp.builder.OnSearchBuilder;
import com.protean.dsep.bpp.builder.OnStatusBuilder;
import com.protean.dsep.bpp.sender.Sender;
import com.protean.dsep.bpp.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchService {

	@Autowired
	private Sender sender;

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private OnSearchBuilder onSearchBuilder;

	@Autowired
	private OnInitBuilder onInitBuilder;
	
	@Autowired
	private OnConfirmBuilder onConfirmBuilder;

	@Autowired
	private OnStatusBuilder onStatusBuilder;
	
	@Autowired
	AuditService auditService;
	
	public void send(SearchRequest model) {

		OnSearchRequest onSearchRequest = this.onSearchBuilder.buildOnSearch(model);

		String json = this.jsonUtil.toJsonSnakeCase(onSearchRequest);

		this.auditService.updateTxnAudit(onSearchRequest.getContext().getMessageId(), onSearchRequest.getContext().getTransactionId(), ContextAction.SEARCH.value());
		
		this.auditService.saveAudit(onSearchRequest.getContext(), json);
		
		String url = onSearchRequest.getContext().getBapUri().concat(ContextAction.ON_SEARCH.value());	//Sending on_search response directly to BAP

		log.info("reply with on_search json {}", json);

		this.sender.send(url, new HttpHeaders(), json);
	}

	public void send(InitRequest model) {

		OnInitRequest onInitRequest = this.onInitBuilder.buildOnInit(model);

		String json = this.jsonUtil.toJsonSnakeCase(onInitRequest);

		this.auditService.updateTxnAudit(onInitRequest.getContext().getMessageId(), onInitRequest.getContext().getTransactionId(), ContextAction.INIT.value());
		
		this.auditService.saveAudit(onInitRequest.getContext(), json);
		
		String url = onInitRequest.getContext().getBapUri().concat(ContextAction.ON_INIT.value());

		log.info("reply with on_init json {}", json);

		this.sender.send(url, new HttpHeaders(), json);
	}
	
	public void send(ConfirmRequest model) {

		OnConfirmRequest onConfirmRequest = this.onConfirmBuilder.buildOnConfirm(model);

		String json = this.jsonUtil.toJsonSnakeCase(onConfirmRequest);
		
		this.auditService.updateTxnAudit(onConfirmRequest.getContext().getMessageId(), onConfirmRequest.getContext().getTransactionId(), ContextAction.CONFIRM.value());
		
		this.auditService.saveAudit(onConfirmRequest.getContext(), json);
		
		String url = onConfirmRequest.getContext().getBapUri().concat(ContextAction.ON_CONFIRM.value());
		log.info("reply with on_confirm json {}", json);

		this.sender.send(url, new HttpHeaders(), json);
	}
	
	public void send(StatusRequest model) {

		OnStatusRequest onStatusRequest = this.onStatusBuilder.buildOnStatus(model);

		String json = this.jsonUtil.toJsonSnakeCase(onStatusRequest);
		
		this.auditService.updateTxnAudit(onStatusRequest.getContext().getMessageId(), onStatusRequest.getContext().getTransactionId(), ContextAction.STATUS.value());
		
		this.auditService.saveAudit(onStatusRequest.getContext(), json);
		
		String url = onStatusRequest.getContext().getBapUri().concat(ContextAction.ON_STATUS.value());
		log.info("reply with on_status json {}", json);

		this.sender.send(url, new HttpHeaders(), json);
	}
}
