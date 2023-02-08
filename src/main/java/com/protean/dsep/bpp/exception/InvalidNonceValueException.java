package com.protean.dsep.bpp.exception;

public class InvalidNonceValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public InvalidNonceValueException() {
		super();
	}

	public InvalidNonceValueException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidNonceValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidNonceValueException(String message) {
		super(message);
	}

	public InvalidNonceValueException(Throwable cause) {
		super(cause);
	}

}
