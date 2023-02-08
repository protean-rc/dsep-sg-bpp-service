package com.protean.dsep.bpp.entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name="dsep_api_audit_seller")
@NamedQuery(name="DsepApiAuditSeller.findAll", query="SELECT d FROM DsepApiAuditSeller d")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
public class DsepApiAuditSeller implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String action;

	@Column(name="buyer_id")
	private String buyerId;

	@Column(name="core_version")
	private String coreVersion;

	@Column(name="created_on")
	private Timestamp createdOn;

	@Column(name="updated_on")
	private Timestamp updatedOn;
	
	private String domain;

	@Type(type = "json")
	private String message;

	@Column(name="message_id")
	private String messageId;

	@Column(name="seller_id")
	private String sellerId;

	private String status;

	@Column(name="transaction_id")
	private String transactionId;

	@Version
	@Column(name="version")
	private Long version;
}