package com.protean.dsep.bpp.model;

import lombok.Data;

import java.util.UUID;

import com.protean.beckn.api.model.common.Customer;
import com.protean.beckn.api.model.common.Order;


@Data
public class ApplicationDtlsViewModel {

	private UUID id;
	private String appId;
	private int appStatus;
	private Customer applcntDtls;
	private String applcntId;
	private String addtnlDtls;
	private String addtnlInfoId;
	private String addtnlInfoSubmsnId;
	private boolean deleted;
	private String dsepTxnId;
	private String remarks;
	private String schemeId;
	private String schemeProviderId;
	private String xinputNonceVal;
	private boolean addtnlInfoReq;
	private String financialYear;
	private boolean published;
	private long schemeAmount;
	private String schemeDescription;
	private String schemeFor;
	private String schemeName;
	private String schemeType;
	private String spocEmail;
	private String spocName;
	private String helpdeskNo;
	private String schemeStartDate;
	private String schemeEndDate;
	private boolean schemeProviderActive;
	private String schemeProviderDescription;
	private String schemeProviderName;
	private String schemeProviderWebsite;
	private String appCreatedAt;
	private String appCreatedBy;
	private String appCreatedIp;
	private String appUpdatedAt;
	private String appUpdatedBy;
	private String appUpdatedIp;

}