package com.protean.dsep.bpp.model;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class SchemeProviderModel {
	private UUID id;
	private String schemeProviderId;
	private String schemeProviderName;
	private String schemeProviderWebsite;
	private String schemeProviderDescription;
	private boolean isActive;
	private boolean isDeleted;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdIP;
	private String updatedIP;
}
