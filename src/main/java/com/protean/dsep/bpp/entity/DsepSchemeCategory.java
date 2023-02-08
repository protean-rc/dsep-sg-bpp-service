package com.protean.dsep.bpp.entity;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name="dsep_scheme_category")
@NamedQuery(name="DsepSchemeCategory.findAll", query="SELECT d FROM DsepSchemeCategory d")
@Data
public class DsepSchemeCategory extends AuditModel {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="dsc_cat_id")
	private Integer dscCatId;

	@Column(name="dsc_cat_code")
	private String dscCatCode;

	@Column(name="dsc_cat_name")
	private String dscCatName;

	@Column(name="dsc_cat_parent_id")
	private Integer dscCatParentId;

}