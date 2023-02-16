package com.protean.dsep.bpp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.protean.dsep.bpp.config.TokenProvider;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.model.AuthTokenModel;
import com.protean.dsep.bpp.model.UserModel;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value = InternalConstant.CONTEXT_ROOT)
public class SecurityController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenProvider tokenProvider;

	@GetMapping(value="check")
	public String check() {
		return "DSEP BPP service is running successfully!";
	}
	
	@PostMapping(value = "/token")
	public ResponseEntity<AuthTokenModel> getToken(@RequestBody UserModel model) throws AuthenticationException {

		log.info("In getToken with user {}", model.toString());

		final Authentication authentication = this.authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						model.getUserId(),
						model.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		final String token = this.tokenProvider.generateToken(authentication);
		return ResponseEntity.ok(new AuthTokenModel(token));
	}

}
