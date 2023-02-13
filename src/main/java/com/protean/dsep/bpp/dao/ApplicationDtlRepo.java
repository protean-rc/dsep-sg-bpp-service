package com.protean.dsep.bpp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.DsepApplicationDtl;

@Repository
public interface ApplicationDtlRepo extends JpaRepository<DsepApplicationDtl, String> {

	DsepApplicationDtl findByDadAppIdAndDadAddtnlInfoId(String appID, String addtnlInfoID);
	
	DsepApplicationDtl findByDadXinputNonceVal(String nonceVal);
	
	DsepApplicationDtl findByDadAppId(String appID);
	
	List<DsepApplicationDtl> findByDadDeletedOrderByUpdatedAtDesc(boolean isDeleted);

	List<DsepApplicationDtl> findByDadAppStatusNotAndDadDeletedOrderByUpdatedAtDesc(int status, boolean isDeleted); 
	
	DsepApplicationDtl findByDadDsepTxnId(String txnID);
}
