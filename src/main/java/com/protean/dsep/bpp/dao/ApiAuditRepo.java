package com.protean.dsep.bpp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.DsepApiAuditSeller;

@Repository
public interface ApiAuditRepo extends JpaRepository<DsepApiAuditSeller, String> {
	DsepApiAuditSeller findByMessageIdAndTransactionIdAndAction(String msgID, String txnID, String actn);
	
	DsepApiAuditSeller findFirstByMessageIdAndTransactionIdAndActionOrderByCreatedOnDesc(String msgID, String txnID, String actn);
}
