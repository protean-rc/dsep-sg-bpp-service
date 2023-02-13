package com.protean.dsep.bpp.util;

import java.util.Map;

import javax.security.auth.message.AuthStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.protean.dsep.bpp.exception.ErrorCode;
import com.protean.dsep.bpp.exception.HeaderValidationFailedException;
import com.protean.dsep.bpp.exception.InvalidUserException;
import com.protean.dsep.bpp.exception.SignatureVerificationFailedException;
import com.protean.dsep.bpp.model.HeaderParams;
import com.protean.dsep.bpp.model.KeyIdDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityUtil {

	@Value("${beckn.seller.algo}")
	private String algo;
	
	@Value("${beckn.seller.uniqKeyId}")
	private String uniqKeyId;
	
	@Value("${beckn.seller.id}")
	private String subID;
	
	@Value("${beckn.seller.private-key}")
	private String privateKey;
	
	@Value("${beckn.seller.public-key}")
	private String publicKey;
	
	@Value("${beckn.req.expiry.time-in-sec}")
	private long requestExpiryTimePeriodInSec;
	
	public String getCurrentUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		log.info("Credentials-{}",authentication.getCredentials());
		log.info("Details-{}",authentication.getDetails());
		log.info("Principal-{}",authentication.getPrincipal());
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUserName = authentication.getName();
			return currentUserName;
		}
		return null;
	}

	public String getRemoteIPAddr() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		log.info("Principal-{}",authentication.getPrincipal());
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			WebAuthenticationDetails webAuthDtls = (WebAuthenticationDetails)authentication.getDetails();
			String ipAddr = webAuthDtls.getRemoteAddress();
			return ipAddr;
		}
		return null;
	}
	
	public boolean authorizeHeader(HttpHeaders httpHeaders, String requestBody) throws InvalidUserException {
		log.info("Validating request header - {}", httpHeaders);
		boolean isValid = true;
		
		String accept = httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
		String authHeader = null;
		Map<String, String> authMap = null;
		//AuthStatus auth = null;
		KeyIdDto keyIdDto = null;

		if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
			// #Auth Header validation
			log.info("#Auth Header validation");
			authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
			
			try {
				
				if(authHeader == null || authHeader.trim().length() == 0) {
					log.error("1.AUTH_HEADER_NOT_FOUND");
					isValid = false;
					throw new InvalidUserException(ErrorCode.AUTH_HEADER_NOT_FOUND.getCode());
				}
				
			} catch (Exception e) {
				log.error("2.AUTH_HEADER_NOT_FOUND - ",e);
				isValid = false;
				throw new InvalidUserException(ErrorCode.AUTH_HEADER_NOT_FOUND.getCode());
			}

			try {
				authMap = SigningUtil.parseAuthorizationHeader(authHeader);
				if (authMap == null && authMap.size() < 1) {
					log.error("1.INVALID_AUTH_HEADER");
					isValid = false;
					throw new InvalidUserException(ErrorCode.INVALID_AUTH_HEADER.getCode());
				}
			} catch (Exception e) {
				log.error("2.INVALID_AUTH_HEADER - ",e);
				isValid = false;
				throw new InvalidUserException(ErrorCode.INVALID_AUTH_HEADER.getCode());
			}

			// #KeyId validation
			log.info("Key Id : " + authMap.get("keyId") + " signature: " + authMap.get("signature"));
			try {
				keyIdDto = SigningUtil.splitKeyId(authMap.get("keyId"));
				if (keyIdDto == null || keyIdDto.getSubId() == null || keyIdDto.getSubId().isEmpty()
						|| keyIdDto.getUkid() == null || keyIdDto.getUkid().isEmpty() || keyIdDto.getAlgo() == null
						|| keyIdDto.getAlgo().isEmpty()) {
					log.error("1.INVALID_KEY_ID_HEADER");
					isValid = false;
					throw new InvalidUserException(ErrorCode.INVALID_KEY_ID_HEADER.getCode());
				}
				log.info("keyIdDto : {}", keyIdDto);
			} catch (Exception e) {
				log.error("2.INVALID_KEY_ID_HEADER - ",e);
				isValid = false;
				throw new InvalidUserException(ErrorCode.INVALID_KEY_ID_HEADER.getCode());
			}

			// #Algo Validation
			String algoParam = authMap.get("algorithm");
			log.info("algorithm : " + algoParam);
			if (!(algo.equals(keyIdDto.getAlgo()) && algoParam != null
					&& algo.equals(algoParam.replace("\"", "")))) {
				log.error("ALGORITHM_MISMATCH");
				isValid = false;
				throw new InvalidUserException(ErrorCode.ALGORITHM_MISMATCH.getCode());
			}

			// #Headers param validation
			String headers = authMap.get("headers");
			HeaderParams headerParams;
			try {
				headerParams = SigningUtil.splitHeadersParam(headers, authMap);
				//headerParams = SigningUtil.splitHeadersParam(headers);
				if (headerParams == null) {
					log.error("1.INVALID_HEADERS_PARAM");
					isValid = false;
					throw new InvalidUserException(ErrorCode.INVALID_HEADERS_PARAM.getCode());
				}
			} catch (HeaderValidationFailedException e1) {
				log.error("2.INVALID_HEADERS_PARAM : ", e1);
				isValid = false;
				throw new InvalidUserException(ErrorCode.INVALID_HEADERS_PARAM.getCode());
			}

			// #Timestamp Expiry check
			if (!validateTS(authMap.get("created"), authMap.get("expires"))) {
				log.error("REQUEST_EXPIRED (TS VAlidation failed)");
				isValid = false;
				throw new InvalidUserException(ErrorCode.REQUEST_EXPIRED.getCode());
			}

			SigningUtil signingUtility = new SigningUtil();

			// #Diagest Validation
			String reqBleckHash = signingUtility.generateBlakeHash(requestBody);

			log.info(" keyIdDto.getSubId() " + keyIdDto.getSubId() + " keyIdDto.getUkid() " + keyIdDto.getUkid());
			// # subscriber id validation
			if (keyIdDto.getSubId() == null || !subID.equals(keyIdDto.getSubId().trim())) {
				isValid = false;
				log.error("AUTH_FAILED : subscriber id validation failed");
				throw new InvalidUserException(ErrorCode.SUBSCRIBER_NOT_FOUND.getCode());
			}

			if (keyIdDto.getUkid() == null || !uniqKeyId.equals(keyIdDto.getUkid().trim())) {
				isValid = false;
				log.error("AUTH_FAILED : unique key id validation failed");
				throw new InvalidUserException(ErrorCode.INVALID_KEY_ID_HEADER.getCode());
			}
			
			// #Signature Verification
			// Signing String generation
			StringBuffer sb = new StringBuffer();
			sb.append("(created): ");
			sb.append(authMap.get("created").replace("\"", ""));
			sb.append("\n");
			sb.append("(expires): ");
			sb.append(authMap.get("expires").replace("\"", ""));
			sb.append("\n");
			sb.append("digest: ");
			sb.append("BLAKE-512=" + reqBleckHash);

			try {
				if (!signingUtility.verifySignature(authMap.get("signature").replace("\"", ""), sb.toString(),
						publicKey)) {
					isValid = false;
					log.error("1.SIGNATURE_VERIFICATION_FAILED");
					throw new InvalidUserException(ErrorCode.SIGNATURE_VERIFICATION_FAILED.getCode());
				}
			} catch (SignatureVerificationFailedException e) {
				isValid = false;
				log.error("2.SIGNATURE_VERIFICATION_FAILED");
				throw new InvalidUserException(ErrorCode.SIGNATURE_VERIFICATION_FAILED.getCode());
			}

			log.info("--------------------------------------------");
			log.info(" request keyIdDto :: " + keyIdDto + "\n request headerParams :: "
					+ headerParams);
		} else {
			isValid = false;
			log.error("BAD_REQUEST :  Accept header mismatch");
			throw new InvalidUserException(ErrorCode.INVALID_CONTENT_TYPE.getCode());
		}
		log.info("@@ inside Request Authentication -> END");
		
		return isValid;
	}
	
	private static boolean validateTS(String crt, String exp) {
		log.info("-----");

		boolean isValid = false;
		try {
			log.info("Created Before : " + crt);
			log.info("Expiry Before : " + exp);

			if (crt != null && exp != null) {
				crt = crt.replace("\"", "");
				exp = exp.replace("\"", "");
				long created = Long.parseLong(crt);
				long expiry = Long.parseLong(exp);
				long now = System.currentTimeMillis() / 1000L;
				long diffInSec = ((expiry - created));
				log.info("Time Difference in seconds : " + diffInSec);
				// System.out.println("Now Time : " + now);

				log.info("Now     : " + now);
				log.info("Created : " + created);
				log.info("Expiry  : " + expiry);

				if (!(diffInSec <= 0 || created > now || expiry <= now || expiry < created))
					isValid = true;
			} else {
				log.error("created or expires timestamp value is null.");
			}

		} catch (Exception e) {
			isValid = false;
		}
		System.out.println("Is Valid  : " + isValid);
		return isValid;
	}
	
	public HttpHeaders generateAuthHeader(String request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		SigningUtil signingUtil = new SigningUtil();

		String reqBleckHash = signingUtil.generateBlakeHash(request);
		
		long creationTime = System.currentTimeMillis() / 1000L;

		String kid = subID + "|" + uniqKeyId + "|" + algo;

		// String header = "(" + creationTime + ") (" + (creationTime +
		// requestExpiryTimePeriodInSec)+ ") BLAKE-512=" + reqBleckHash + "";
		
		//String header = "(created) (expires) digest";

		String header = "(" + creationTime + ") ("+ (creationTime + requestExpiryTimePeriodInSec) +") digest";

		String signingString = "(created): "+creationTime+"\n(expires): "+(creationTime + requestExpiryTimePeriodInSec)+"\ndigest: BLAKE-512="+reqBleckHash+"";
		
		String signedReq = signingUtil.generateSignature(signingString, privateKey);

		// headers.add("Authorization", "Signature keyId=\"" + kid + "\",algorithm=\"" +
		// nsdlProviderAlgo + "\", headers=\"" + header + "\", signature=\"" + signedReq
		// + "\"");
		/*String auth ="Signature keyId=\"" + kid + "\", created=\"" + creationTime + "\", expires=\""
				+ (creationTime + 60000) + "\", algorithm=\"" + algo + "\", headers=\""
				+ header + "\", signature=\"" + signedReq + "\"";*/
		
		headers.set(HttpHeaders.AUTHORIZATION,
				"Signature keyId=\"" + kid + "\", created=\"" + creationTime + "\", expires=\""
						+ (creationTime + 60000) + "\", algorithm=\"" + algo + "\", headers=\""
						+ header + "\", signature=\"" + signedReq + "\"");
		
		log.info("Generated Auth Header ==> {} ", headers);
		
		return headers;
	}
}
