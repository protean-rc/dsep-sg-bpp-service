package com.protean.dsep.bpp.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class CommonUtil {

	String id = null;

	public String generateID(String prefix, String format) {
		long number = Long.valueOf(RandomStringUtils.randomNumeric(8));
		id = String.format(format, prefix, number);
		return id;
	}

	public String generateNonceVal(int size) {
		String nonceVal = null;
		SecureRandom secureRandom = new SecureRandom();
		byte[] nonceArr = new byte[size];
		secureRandom.nextBytes(nonceArr);
		nonceVal = Base64.getEncoder().encodeToString(nonceArr);
		return nonceVal;
	}
}
