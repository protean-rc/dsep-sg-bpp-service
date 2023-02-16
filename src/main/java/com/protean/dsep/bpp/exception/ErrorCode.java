package com.protean.dsep.bpp.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
	SUBSCRIBER_NOT_FOUND(1000, "Subscriber not found"),
	AUTH_FAILED(1005, "Authentication failed"),
	INVALID_KEY_ID_HEADER(1010, "Invalid keys are found in the header"),
	INVALID_AUTH_HEADER(1015, "The auth header is not valid"),
	AUTH_HEADER_NOT_FOUND(1020, "The auth header not found"),
	BAD_REQUEST(1025, "Bad request"),
	ALGORITHM_MISMATCH(1030, "There is mismatch in the algorithm"),
	REQUEST_EXPIRED(1035, "The request has expired"),
	INVALID_HEADERS_PARAM(1040, "Invalid headers are present in header parameters"),
	SIGNATURE_VERIFICATION_FAILED(1045, "The signature verification failed"),
	REQ_DIGEST_MISMATCH(1050, "Request digest mismatch"),
	INVALID_REQUEST(1055, "Invalid request"),
	REQUEST_ALREADY_IN_PROCESS(1060, "The request is already in process"),
	INVALID_DOMAIN(1065, "Invalid domain"),
	NO_GATEWAY_FOUND(1066, "No gateway found for the requested domain"),
	UNKNOWN_ERROR(1070, "The Service faced a fatal technical exception"),
	REQUEST_ALREADY_PROCESSED(1075, "The request is already processed"),
	REQUESTED_TRANSACTION_IS_NOT_EXIST(1080, "Transaction id in the request in not valid"),
	INVALID_ACTION(1085, "Invalid action"),
	INVALID_CONTENT_TYPE(1090, "Invalid content type. Only application/json allowed"),
	HEADER_SEQ_MISMATCH(1095, "Header sequence mismatched"),
	HEADER_PARSING_FAILED(1100, "Header parsing failed"),
	INVALID_BPP(1105, "BPP is not valid/not registered in system"),
	JSON_PROCESSING_ERROR(1110, "Issue while preparing the json"),
	INVALID_ENTITY_TYPE(1115, "Invalid entity type configured in config file"),
	HTTP_TIMEOUT_ERROR(1120, "Http timeout error"),
	NETWORK_ERROR(1125, "Issue while making http call"),
	CERTIFICATE_ALIAS_ERROR(1130, "Required alias not found in the certificate"),
	CERTIFICATE_ERROR(1135, "Error while loading the certificate"),
	SIGNATURE_ERROR(1140, "Error while generating the signature"),

	// proxy header error
	PROXY_HEADER_NOT_FOUND(2000, "The proxy header not found"),
	PROXY_AUTH_FAILED(2001, "Proxy Authentication failed"),
	INVALID_PROXY_KEY_ID_HEADER(2002, "Invalid proxy keyid is found in the header"),
	INVALID_PROXY_AUTH_HEADER(2003, "The proxy auth header is not valid"),
	PROXY_ALGORITHM_MISMATCH(2005, "There is mismatch in the algorithm of proxy header"),
	PROXY_REQUEST_EXPIRED(2006, "The proxy request has expired"),
	INVALID_PROXY_HEADERS_PARAM(2007, "Invalid headers are present in proxy header parameters");

	private int code;
	private String message;
	
	public static final Map<String, String> CODE_MSG = new HashMap<>();

	static {
		for (ErrorCode c : values()) {
			CODE_MSG.put(String.valueOf(c.code), c.message);
		}
	}
	
	ErrorCode(final int errorCode, final String errorMessage) {
		this.code = errorCode;
		this.message = errorMessage;
	}

	public int getCode() { return this.code; }

	public void setCode(final int errorCode) { this.code = errorCode; }

	public String getMessage() { return this.message; }

	public void setMessage(final String errorMessage) { this.message = errorMessage; }
}