package com.protean.dsep.bpp.model;

import java.util.UUID;

import com.protean.beckn.api.model.common.Customer;
import com.protean.beckn.api.model.common.Order;

import lombok.Data;

@Data
public class ApplicationDtlModel {

	private UUID id;
	private String dsepTxnId;
	private String addtnlDtls;
	private String addtnlInfoId;
	private String addtnlInfoSubmsnId;
	private String appId;
	private int appStatus;
	private String applcntId;
	private Customer applcntDtls;
	private boolean deleted;
	private String schemeId;
	private String schemeProviderId;
	private String xinputNonceVal;
	private String remarks;
	private String createdAt;
	private String updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdIP;
	private String updatedIP;
}
