package com.protean.dsep.bpp.model;

import lombok.Data;

@Data
public class SchemeCategoryModel {
	private int catId;
	private String catCode;
	private String catName;
	private int catParentId;
}
