package com.protean.dsep.bpp.exception;

public class InvalidXInputSubmissionIDException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public InvalidXInputSubmissionIDException() {
		super();
	}

	public InvalidXInputSubmissionIDException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidXInputSubmissionIDException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidXInputSubmissionIDException(String message) {
		super(message);
	}

	public InvalidXInputSubmissionIDException(Throwable cause) {
		super(cause);
	}

}
