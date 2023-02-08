package com.protean.dsep.bpp.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDetails {
	private String code;
	private String status;
	private String message;
}
