package com.protean.dsep.bpp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XInputDataModel {
	private String name;
	private String phone;
	private String address;
	
	@JsonProperty("needOfScholarship")
	private String needOfScholarship;
	
	@JsonProperty("docUrl")
	private String docUrl;
}
