package com.protean.dsep.bpp.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityUtil {

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
}
