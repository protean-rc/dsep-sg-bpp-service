package com.protean.dsep.bpp.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "user_roles")
@Data
public class UserRolesEntity extends AuditModel  {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UserRolesEntityPk pk = new UserRolesEntityPk();

	@ToString.Exclude
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
	private UserEntity userEntity;

	@Embeddable
	@Data
	public static class UserRolesEntityPk implements Serializable {

		private static final long serialVersionUID = -275260647633971554L;

		@Column(name = "user_id")
		private String userId;

		@Column(name = "role_id")
		private String role;
	}

}
