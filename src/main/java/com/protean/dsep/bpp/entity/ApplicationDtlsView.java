package com.protean.dsep.bpp.entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.Data;

import java.util.Date;
import java.util.UUID;
import java.sql.Timestamp;

@Entity
@Table(name="application_dtls_view")
@NamedQuery(name="ApplicationDtlsView.findAll", query="SELECT a FROM ApplicationDtlsView a")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
public class ApplicationDtlsView implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="dad_id")
	private UUID dadId;
	
	@Column(name="dad_app_id")
	private String dadAppId;

	@Column(name="dad_app_status")
	private Integer dadAppStatus;

	@Column(name="dad_applcnt_dtls")
	@Type(type = "json")
	private String dadApplcntDtls;

	@Column(name="dad_applcnt_id")
	private String dadApplcntId;

	@Column(name="dad_addtnl_dtls")
	@Type(type = "json")
	private String dadAddtnlDtls;

	@Column(name="dad_addtnl_info_id")
	private String dadAddtnlInfoId;

	@Column(name="dad_addtnl_info_submsn_id")
	private String dadAddtnlInfoSubmsnId;

	@Column(name="dad_deleted")
	private Boolean dadDeleted;

	@Column(name="dad_dsep_txn_id")
	private String dadDsepTxnId;

	@Column(name="dad_remarks")
	private String dadRemarks;

	@Column(name="dad_scheme_id")
	private String dadSchemeId;

	@Column(name="dad_scheme_provider_id")
	private String dadSchemeProviderId;

	@Column(name="dad_xinput_nonce_val")
	private String dadXinputNonceVal;

	@Column(name="ds_addtnl_info_req")
	private Boolean dsAddtnlInfoReq;

	@Column(name="ds_financial_year")
	private String dsFinancialYear;

	@Column(name="ds_is_published")
	private Boolean dsIsPublished;

	@Column(name="ds_scheme_amount")
	private Long dsSchemeAmount;

	@Column(name="ds_scheme_description")
	private String dsSchemeDescription;

	@Column(name="ds_scheme_for")
	private String dsSchemeFor;

	@Column(name="ds_scheme_name")
	private String dsSchemeName;

	@Column(name="ds_scheme_type")
	private String dsSchemeType;

	@Column(name="ds_spoc_email")
	private String dsSpocEmail;

	@Column(name="ds_spoc_name")
	private String dsSpocName;

	@Column(name="ds_helpdesk_no")
	private String dsHelpdeskNo;

	@Temporal(TemporalType.DATE)
	@Column(name="ds_start_date")
	private Date dsStartDate;

	@Temporal(TemporalType.DATE)
	@Column(name="ds_end_date")
	private Date dsEndDate;

	@Column(name="dsp_is_active")
	private Boolean dspIsActive;

	@Column(name="dsp_scheme_provider_description")
	private String dspSchemeProviderDescription;

	@Column(name="dsp_scheme_provider_name")
	private String dspSchemeProviderName;

	@Column(name="dsp_scheme_provider_website")
	private String dspSchemeProviderWebsite;

	@Column(name="created_at")
	private Timestamp createdAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="created_ip")
	private String createdIp;
	
	@Column(name="updated_at")
	private Timestamp updatedAt;

	@Column(name="updated_by")
	private String updatedBy;

	@Column(name="updated_ip")
	private String updatedIp;

	private Long version;

}