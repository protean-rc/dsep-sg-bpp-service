package com.protean.dsep.bpp.entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.Data;

import java.sql.Timestamp;


@Entity
@Table(name="dsep_api_audit_seller_error")
@NamedQuery(name="DsepApiAuditSellerError.findAll", query="SELECT d FROM DsepApiAuditSellerError d")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
public class DsepApiAuditSellerError implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="created_on")
	private Timestamp createdOn;

	private String error;

	@Type(type = "json")
	private String json;

	@Column(name="schema_class")
	private String schemaClass;
}