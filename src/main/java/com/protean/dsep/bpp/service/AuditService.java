package com.protean.dsep.bpp.service;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.protean.beckn.api.model.common.Context;
import com.protean.dsep.bpp.dao.ApiAuditRepo;
import com.protean.dsep.bpp.entity.DsepApiAuditSeller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuditService {

	@Autowired
	ApiAuditRepo apiAuditRepo;
	
	public void saveAudit(Context ctx, String body) {
		log.info("Saving API audit record | Context-{} | body-{}",ctx,body);
		try {
			DsepApiAuditSeller apiAuditEntity = buildApiAuditEntity(ctx,body);
			apiAuditRepo.save(apiAuditEntity);
		} catch (Exception e) {
			log.error("Exception occurred while saving API audit record:",e);
		}
	}
	
	public void updateTxnAudit(String msgID, String txnID, String actn) {
		log.info("Updating API audit record | MessageID-{} | TransactionID-{} | Action-{}",msgID,txnID,actn);
		try {
			//DsepApiAuditSeller apiAuditEntity = apiAuditRepo.findByMessageIdAndTransactionIdAndAction(msgID, txnID, actn);
			DsepApiAuditSeller apiAuditEntity = apiAuditRepo.findFirstByMessageIdAndTransactionIdAndActionOrderByCreatedOnDesc(msgID, txnID, actn);
			apiAuditEntity.setStatus("1");
			apiAuditEntity.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
			apiAuditRepo.save(apiAuditEntity);
		} catch (Exception e) {
			log.error("Exception occurred while saving API audit record:",e);
		}
	}
	
	private DsepApiAuditSeller buildApiAuditEntity (Context ctx, String body) {
		DsepApiAuditSeller entity = new DsepApiAuditSeller();
		entity.setId(UUID.randomUUID().toString());
		entity.setTransactionId(ctx.getTransactionId());
		entity.setAction(ctx.getAction());
		entity.setBuyerId(ctx.getBapId());
		entity.setSellerId(ctx.getBppId());
		entity.setCoreVersion(ctx.getVersion());
		entity.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		entity.setDomain(ctx.getDomain());
		entity.setMessageId(ctx.getMessageId());
		entity.setMessage(body);
		entity.setStatus("0");
		return entity;
	}
}
