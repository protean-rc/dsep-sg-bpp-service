package com.protean.dsep.bpp.model;

import java.util.UUID;

import lombok.Data;

@Data
public class SchemeModel {

	private UUID id;
	private String schemeID;
	private String schemeName;
	private String schemeDescription;
	private String schemeType;
	private String schemeProviderID;
	private String schemeFor;
	private String financialYear;
	private long schemeAmount;
	private String startDate;
	private String endDate;
	private SchemeEligibilityModel eligibility;
	private boolean addtnlInfoReq;
	private String spocName;
	private String spocEmail;
	private String helpdeskNo;
	private boolean isPublished;
	private boolean isDeleted;
	private String createdAt;
	private String updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdIP;
	private String updatedIP;

}
