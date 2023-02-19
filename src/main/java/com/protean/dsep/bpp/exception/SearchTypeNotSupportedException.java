package com.protean.dsep.bpp.exception;

public class SearchTypeNotSupportedException extends Exception {

	private static final long serialVersionUID = 1L;

	public SearchTypeNotSupportedException(Integer msg) {
		super(msg.toString());
	}
	
	public SearchTypeNotSupportedException(String msg) {
		super(msg);
	}
}
