package com.protean.dsep.bpp.util;

import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import com.protean.dsep.bpp.exception.HeaderValidationFailedException;
import com.protean.dsep.bpp.exception.SignatureVerificationFailedException;
import com.protean.dsep.bpp.model.HeaderParams;
import com.protean.dsep.bpp.model.KeyIdDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SigningUtil {	
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public String generateSignature(String req, String pk) {
		String signature = null;
		try {
			
			Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(Base64.getDecoder().decode(pk.getBytes()), 0);
			
			Signer sig = new Ed25519Signer();
	        sig.init(true, privateKey);
	        sig.update(req.getBytes(), 0, req.length());
			
	        byte[] s1 = sig.generateSignature();
	        
	        signature = Base64.getEncoder().encodeToString(s1);
	        
	        log.info("Signature Generated From Data : " + signature);
	        
		} catch (DataLengthException | CryptoException e) {
			log.error("Exception occured while generating signature -",e);
		}
		return signature;
	}
	
	public boolean verifySignature(String sign, String requestData, String dbPublicKey) throws SignatureVerificationFailedException {
		boolean isVerified = false;
		
		try {
			log.info("==performing signature verification==");
			log.info("sign "+ sign);
			log.info("requestData \n"+requestData);
			log.info("dbPublicKey "+dbPublicKey);
			
			Ed25519PublicKeyParameters publicKey = new Ed25519PublicKeyParameters(Base64.getDecoder().decode(dbPublicKey), 0);
			Signer sv = new Ed25519Signer();
			sv.init(false, publicKey);
	        sv.update(requestData.getBytes(), 0, requestData.length());
	        
	        byte[] decodedSign = Base64.getDecoder().decode(sign);
	        isVerified = sv.verifySignature(decodedSign);
			log.info("signature verification result:{}", isVerified);
			
		} catch (Exception e) {
			log.error("Exception occurred while verifying signature-",e);
			throw new SignatureVerificationFailedException(e);
		}
		return isVerified;
	}
	
	
	public static Map<String, String> parseAuthorizationHeader(String authHeader) {
    	Map<String, String> holder = new HashMap<String, String>();
        if(authHeader.contains("Signature ")) {
        	authHeader = authHeader.replace("Signature ","");
        	String[] keyVals = authHeader.split(",");
            for(String keyVal:keyVals)
            {
              String[] parts = keyVal.split("=",2);
              if(parts[0]!=null && parts[1]!=null)
            	  holder.put(parts[0].trim(),parts[1].trim());
            }
            return holder;
        }
    	return null;
    }

	public static KeyIdDto splitKeyId(String kid) throws HeaderValidationFailedException {
		log.info("@inside splitKeyId : {}", kid);
		KeyIdDto keyIdDto  = null;
		
		try {
			if(kid!=null && !kid.isEmpty()) {
				kid = kid.replace("\"", "");
				keyIdDto = new KeyIdDto();
				
				String[] a = kid.split("[|]");
				keyIdDto.setSubId(a[0]);
				keyIdDto.setUkid(a[1]);
				keyIdDto.setAlgo(a[2]);
			}
		} catch (Exception e) {
			log.error("Exception occurred while authorizing header-",e);
			throw new HeaderValidationFailedException("Authorization Header Validation Failed");
		}
		return keyIdDto;
	}
	
	public static HeaderParams splitHeadersParam(String headers) throws HeaderValidationFailedException
	{
		log.info("@inside splitHeadersParam : {}",headers);
		HeaderParams headerParams  = null;
		try {
			if(headers!=null && !headers.isEmpty()) {
				headers = headers.replace("\"", "");
				headerParams = new HeaderParams();
				
				String[] a = headers.split(" ");
				if(a!=null && a.length > 2) {
					headerParams.setCreated(a[0].replace("(", "").replace(")", ""));
					headerParams.setExpires(a[1].replace("(", "").replace(")", ""));
					headerParams.setDiagest(a[2].trim());
					if(!(headerParams.getCreated()!=null && "created".equalsIgnoreCase(headerParams.getCreated()) 
							&& headerParams.getExpires()!=null && "expires".equalsIgnoreCase(headerParams.getExpires())
							&& headerParams.getDiagest()!=null && "digest".equalsIgnoreCase(headerParams.getDiagest()))) {
						log.error("Header sequense mismatch");
						throw new HeaderValidationFailedException("Header sequense mismatch");
					}
				}else {
					log.error("Invalid Header");
					throw new HeaderValidationFailedException("Invalid Header");
				}
				
			}
		} catch (Exception e) {
			log.error("Exception occurred while authorizing header parameters-",e);
			throw new HeaderValidationFailedException("Header parsing Failed");
		}
		
		return headerParams;
	}
	
	public String generateBlakeHash(String req) {
		Blake2bDigest blake2bDigest = new Blake2bDigest(512);
    	
    	byte[] test = req.getBytes();
    	blake2bDigest.update(test, 0, test.length);
    	
    	byte[] hash = new byte[blake2bDigest.getDigestSize()];
    	blake2bDigest.doFinal(hash, 0);
    	
    	String hex = Hex.toHexString(hash);
    	log.info("Hex : {}", hex);
    	String bs64 = Base64.getUrlEncoder().encodeToString(hex.getBytes());
    	log.info("Base64 URL Encoded : {}", bs64);
		return bs64;
	}
}
