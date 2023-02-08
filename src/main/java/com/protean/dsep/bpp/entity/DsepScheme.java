package com.protean.dsep.bpp.entity;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="dsep_schemes")
@NamedQuery(name="DsepScheme.findAll", query="SELECT d FROM DsepScheme d")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
public class DsepScheme extends AuditModel {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
	@Column(name="ds_id", updatable = false, nullable = false)
	private UUID dsId;

	@Column(name="ds_scheme_id")
	private String dsSchemeId;

	@Column(name="ds_scheme_name")
	private String dsSchemeName;

	@Column(name="ds_scheme_description")
	private String dsSchemeDescription;

	@Column(name="ds_scheme_type")
	private String dsSchemeType;
	
	@Column(name="ds_scheme_provider_id")
	private String dsSchemeProviderId;
	
	@Column(name="ds_scheme_for")
	private String dsSchemeFor;
	
	@Column(name="ds_financial_year")
	private String dsFinancialYear;
	
	@Column(name="ds_scheme_amount")
	private Long dsSchemeAmount;

	@Temporal(TemporalType.DATE)
	@Column(name="ds_start_date")
	private Date dsStartDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name="ds_end_date")
	private Date dsEndDate;

	@Column(name="ds_eligibility")
	@Type(type = "json")
	private String dsEligibility;

	@Column(name="ds_addtnl_info_req")
	private boolean dsAddtnlInfoReq;
	
	@Column(name="ds_spoc_name")
	private String dsSpocName;

	@Column(name="ds_spoc_email")
	private String dsSpocEmail;

	@Column(name="ds_helpdesk_no")
	private String dsHelpdeskNo;
	
	@Column(name="ds_is_published")
	private boolean dsIsPublished;
	
	@Column(name="ds_is_deleted")
	private boolean dsIsDeleted;

}