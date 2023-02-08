package com.protean.dsep.bpp.model;

import java.util.Date;

import lombok.Data;

@Data
public class UserRoleModel {
	private String emailId;
	private String role;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdIP;
	private String updatedIP;
	private long version;
}
