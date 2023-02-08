package com.protean.dsep.bpp.exception;

public class UserAlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserAlreadyRegisteredException() {
		super();
	}

	public UserAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UserAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserAlreadyRegisteredException(String message) {
		super(message);
	}

	public UserAlreadyRegisteredException(Throwable cause) {
		super(cause);
	}
	
	

}
