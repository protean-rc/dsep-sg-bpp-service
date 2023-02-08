package com.protean.dsep.bpp.entity;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity extends AuditModel {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "password")
	private String password;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "location")
	private String location;

	@OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<UserRolesEntity> userRoles;

	@Column(name = "provider_id")
	private UUID providerId;
}
