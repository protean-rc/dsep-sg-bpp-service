package com.protean.dsep.bpp.model;

import java.util.List;

import lombok.Data;

@Data
public class SchemeEligibilityModel {
	private List<AcademicDtlsModel> acadDtls;
	private int age;
	private String gender;
    private String familyIncome;
    private List<String> caste;
}
