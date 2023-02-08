package com.protean.dsep.bpp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.protean.dsep.bpp.builder.ResponseDetails;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.exception.UserAlreadyRegisteredException;
import com.protean.dsep.bpp.model.UserModel;
import com.protean.dsep.bpp.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value = InternalConstant.CONTEXT_ROOT)
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping(value = "/signup")
	public ResponseEntity<Object> saveUser(@RequestBody UserModel model) {
		log.info("Going to register user with data {}", model);
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		try {
			result = this.userService.save(model);
			response = new ResponseEntity<Object>(result,HttpStatus.CREATED);
		} catch (UserAlreadyRegisteredException e) {
			log.error(e.getMessage());
			result = false;
			respDtls = new ResponseDetails(String.valueOf("ERR-101"), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls,HttpStatus.OK);
		} catch (Exception e) {
			log.error("Unknown exception while registering user: {}",e.getMessage());
			result = false;
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

}
