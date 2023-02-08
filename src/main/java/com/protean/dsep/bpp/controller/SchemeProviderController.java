package com.protean.dsep.bpp.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.protean.dsep.bpp.builder.ResponseDetails;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.model.SchemeProviderModel;
import com.protean.dsep.bpp.service.SchemeProviderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(InternalConstant.CONTEXT_ROOT+"/scheme/provider")
@Slf4j
public class SchemeProviderController {
	
	@Autowired
	SchemeProviderService service;
	
	@PostMapping("/add")
	public ResponseEntity<ResponseDetails> createScheme(@RequestBody SchemeProviderModel model) {
		String spID = null;
		ResponseEntity<ResponseDetails> response = null;
		ResponseDetails respDtls = null;
		try {
			log.info("Add scheme provider | Input recieved - {}", model);
			spID = this.service.addSchemeProvider(model);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.CREATED.value()), InternalConstant.SUCCESS, "SchemeProviderID-"+spID);
			response = new ResponseEntity<ResponseDetails>(respDtls, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Exception while adding scheme provider->",e);
			respDtls = new ResponseDetails(InternalConstant.DEFAULT_ERROR_CODE, InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<ResponseDetails>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@GetMapping("/list")
	public ResponseEntity<Object> schemeProviderList() {
		log.info("going to get the scheme provider list");
		ResponseEntity<Object> response = null;
		List<SchemeProviderModel> entityList = null;
		ResponseDetails respDtls = null;
		
		try {
			entityList = this.service.getSchemeProviderList();
			response = new ResponseEntity<Object>(entityList, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while listing schemes->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	@GetMapping("/{schemeProviderID}")
	public ResponseEntity<Object> getSchemeByID(@PathVariable String schemeProviderID) {
		log.info("going to get the details of scheme provider#-{}",schemeProviderID);
		ResponseEntity<Object> response = null;
		SchemeProviderModel spDtls = null;
		ResponseDetails respDtls = null;
		
		try {
			spDtls = this.service.getDetailsBySchemeProviderID(schemeProviderID);
			response = new ResponseEntity<Object>(spDtls, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while getting scheme provider details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme provider not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while getting scheme provider details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/update/{schemeProviderID}")
	public ResponseEntity<Object> updateScheme(@PathVariable String schemeProviderID, @RequestBody SchemeProviderModel model) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("Update scheme provider {} | Input recieved - {}", schemeProviderID, model);
			result = this.service.updateSchemeProvider(schemeProviderID, model);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while updating scheme provider details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme provider not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while updating scheme provider details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/activate/{schemeProviderID}")
	public ResponseEntity<Object> activateSchemeProvider(@PathVariable String schemeProviderID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("going to activate scheme provider{}", schemeProviderID);
			result = this.service.activateDeactivateSchemeProvider(schemeProviderID, Boolean.TRUE);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while activating scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme provider not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while publishing scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/deactivate/{schemeProviderID}")
	public ResponseEntity<Object> deactivateSchemeProvider(@PathVariable String schemeProviderID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("going to deactivate scheme {}", schemeProviderID);
			result = this.service.activateDeactivateSchemeProvider(schemeProviderID, Boolean.FALSE);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while deactivating scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme provider not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while deactivating scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/delete/{schemeProviderID}")
	public ResponseEntity<Object> deleteSchemeProvider(@PathVariable String schemeProviderID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		
		try {
			log.info("going to delete scheme provider {}", schemeProviderID);
			result = this.service.deleteSchemeProvider(schemeProviderID);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while deleting scheme details->",e);
			response = new ResponseEntity<Object>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while deleting scheme details->",e);
			response = new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
}
