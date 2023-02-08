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
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.service.SchemeService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = InternalConstant.CONTEXT_ROOT+"/scheme")
@Slf4j
@Api(value = "Scheme API")
public class SchemeController {

	@Autowired
	private SchemeService service;
	

	@PostMapping("/create")
	public ResponseEntity<ResponseDetails> createScheme(@RequestBody SchemeModel model) {
		String schemeID = null;
		ResponseEntity<ResponseDetails> response = null;
		ResponseDetails respDtls = null;
		try {
			log.info("Create scheme | Input recieved - {}", model);
			schemeID = this.service.createScheme(model);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.CREATED.value()), InternalConstant.SUCCESS, "SchemeID-"+schemeID);
			response = new ResponseEntity<ResponseDetails>(respDtls, HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Exception while creating scheme->",e);
			respDtls = new ResponseDetails(InternalConstant.DEFAULT_ERROR_CODE, InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<ResponseDetails>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@GetMapping("/list")
	public ResponseEntity<Object> schemeList() {
		log.info("going to get the item list for the seller");
		ResponseEntity<Object> response = null;
		List<SchemeModel> entityList = null;
		ResponseDetails respDtls = null;
		
		try {
			entityList = this.service.getSchemeList();
			response = new ResponseEntity<Object>(entityList, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while listing schemes->",e);
			if(e.getMessage() != null && "Invalid Scheme Provider ID".equalsIgnoreCase(e.getMessage().trim())) {
				respDtls = new ResponseDetails(String.valueOf(HttpStatus.BAD_REQUEST.value()), InternalConstant.FAILED, e.getMessage());
				response = new ResponseEntity<Object>(respDtls, HttpStatus.BAD_REQUEST);
			}else {
				respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
				response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		return response;
	}
	
	@GetMapping("/{schemeID}")
	public ResponseEntity<Object> getSchemeByID(@PathVariable String schemeID) {
		log.info("going to get the details of scheme#-{}",schemeID);
		ResponseEntity<Object> response = null;
		SchemeModel schemeDtls = null;
		ResponseDetails respDtls = null;
		
		try {
			schemeDtls = this.service.getDetailsBySchemeID(schemeID);
			response = new ResponseEntity<Object>(schemeDtls, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while getting scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while getting scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/update/{schemeID}")
	public ResponseEntity<Object> updateScheme(@PathVariable String schemeID, @RequestBody SchemeModel model) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("Update scheme {} | Input recieved - {}", schemeID, model);
			result = this.service.updateScheme(schemeID, model);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while updating scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while updating scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/publish/{schemeID}")
	public ResponseEntity<Object> publishScheme(@PathVariable String schemeID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("going to publish scheme {}", schemeID);
			result = this.service.publishUnpublishScheme(schemeID, Boolean.TRUE);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while publishing scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while publishing scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/unpublish/{schemeID}")
	public ResponseEntity<Object> unpublishScheme(@PathVariable String schemeID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("going to unpublish scheme {}", schemeID);
			result = this.service.publishUnpublishScheme(schemeID, Boolean.FALSE);
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while unpublishing scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Scheme not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while unpublishing scheme details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/delete/{schemeID}")
	public ResponseEntity<Object> deleteScheme(@PathVariable String schemeID) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		
		try {
			log.info("going to delete scheme {}", schemeID);
			result = this.service.deleteScheme(schemeID);
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
