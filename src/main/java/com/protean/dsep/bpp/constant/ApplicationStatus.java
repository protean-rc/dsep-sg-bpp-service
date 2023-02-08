package com.protean.dsep.bpp.constant;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationStatus {
	
	INIT(0),
	INPROGRESS(1),
	AWARDED(2),
	DISBURSED(3),
	CLOSED(4),
	REJECTED(5);
	
	private final int value;
	public static final Map<Integer, String> APPSTATUS = new HashMap<Integer, String>();
	
	ApplicationStatus (int value){
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}
	
	static {
		for (ApplicationStatus s : values()) {
			APPSTATUS.put(s.value, s.name());
		}
	}
}
