package com.protean.dsep.bpp.entity;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.Data;
import java.util.UUID;


@Entity
@Table(name="dsep_application_dtls")
@NamedQuery(name="DsepApplicationDtl.findAll", query="SELECT d FROM DsepApplicationDtl d")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
public class DsepApplicationDtl extends AuditModel {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
	@Column(name="dad_id", updatable = false, nullable = false)
	private UUID dadId;

	@Column(name="dad_dsep_txn_id")
	private String dadDsepTxnId;
	
	@Column(name="dad_addtnl_dtls")
	@Type(type = "json")
	private String dadAddtnlDtls;

	@Column(name="dad_addtnl_info_id")
	private String dadAddtnlInfoId;

	@Column(name="dad_addtnl_info_submsn_id")
	private String dadAddtnlInfoSubmsnId;

	@Column(name="dad_app_id")
	private String dadAppId;

	@Column(name="dad_app_status")
	private Integer dadAppStatus;

	@Column(name="dad_applcnt_id")
	private String dadApplcntId;
	
	@Column(name="dad_applcnt_dtls")
	@Type(type = "json")
	private String dadApplcntDtls;

	@Column(name="dad_deleted")
	private Boolean dadDeleted;

	@Column(name="dad_scheme_id")
	private String dadSchemeId;

	@Column(name="dad_scheme_provider_id")
	private String dadSchemeProviderId;

	@Column(name="dad_xinput_nonce_val")
	private String dadXinputNonceVal;

	@Column(name="dad_remarks")
	private String dadRemarks;
}