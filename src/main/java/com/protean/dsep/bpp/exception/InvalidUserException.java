package com.protean.dsep.bpp.exception;

public class InvalidUserException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidUserException(Integer msg) {
		super(msg.toString());
	}
	
	public InvalidUserException(String msg) {
		super(msg);
	}
	
}
