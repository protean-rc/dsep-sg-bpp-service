package com.protean.dsep.bpp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class UserModel {
	private String userId;
	private String password;
	private String fullName;
	private String location;
	private List<UserRoleModel> roles = new ArrayList<>();
	private UUID providerId;
	private String createdAt;
	private String updatedAt;
	private String createdBy;
	private String updatedBy;
	private String createdIP;
	private String updatedIP;
	private long version;
}
