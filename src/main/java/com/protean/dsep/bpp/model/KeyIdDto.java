package com.protean.dsep.bpp.model;

import lombok.Data;

@Data
public class KeyIdDto 
{
	private String subId;
	private String algo;
	private String ukid;
	
	@Override
	public String toString() {
		return "KeyIdDto [subId=" + subId + ", algo=" + algo + ", ukid=" + ukid + "]";
	}
	
	
}
