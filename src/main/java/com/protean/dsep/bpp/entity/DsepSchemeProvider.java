package com.protean.dsep.bpp.entity;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

import java.util.UUID;


@Entity
@Table(name="dsep_scheme_provider")
@NamedQuery(name="DsepSchemeProvider.findAll", query="SELECT d FROM DsepSchemeProvider d")
@Data
public class DsepSchemeProvider extends AuditModel {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
	@Column(name="dsp_id", updatable = false, nullable = false)
	private UUID dspId;

	@Column(name="dsp_scheme_provider_id")
	private String dspSchemeProviderId;

	@Column(name="dsp_scheme_provider_name")
	private String dspSchemeProviderName;
	
	@Column(name="dsp_scheme_provider_website")
	private String dspSchemeProviderWebsite;

	@Column(name="dsp_scheme_provider_description")
	private String dspSchemeProviderDescription;

	@Column(name="dsp_is_Active")
	private boolean dspIsActive;
	
	@Column(name="dsp_is_Deleted")
	private boolean dspIsDeleted;
}