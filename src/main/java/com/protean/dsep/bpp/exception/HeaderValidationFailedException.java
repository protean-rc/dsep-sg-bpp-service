package com.protean.dsep.bpp.exception;

public class HeaderValidationFailedException extends Exception
{
	private static final long serialVersionUID = 1L;

	public HeaderValidationFailedException(String msg) 
	{
		super(msg);
	}
}
