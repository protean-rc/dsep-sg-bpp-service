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
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.ApplicationDtlsViewModel;
import com.protean.dsep.bpp.service.ApplicationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = InternalConstant.CONTEXT_ROOT+"/app")
@Slf4j
public class ApplicationController {
	
	@Autowired
	ApplicationService appService;
	
	@GetMapping("/list")
	public ResponseEntity<Object> applicationList() {
		log.info("Listing applications recieved...");
		ResponseEntity<Object> response = null;
		List<ApplicationDtlsViewModel> entityList = null;
		ResponseDetails respDtls = null;
		
		try {
			entityList = this.appService.getApplicationViewList();
			response = new ResponseEntity<Object>(entityList, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while listing applications->",e);
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
	
	@GetMapping("/{appID}")
	public ResponseEntity<Object> getAppDtlsByAppID(@PathVariable String appID) {
		log.info("going to get the details of Application#-{}",appID);
		ResponseEntity<Object> response = null;
		ApplicationDtlModel appDtls = null;
		ResponseDetails respDtls = null;
		
		try {
			appDtls = this.appService.getDetailsByAppID(appID);
			response = new ResponseEntity<Object>(appDtls, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while getting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Application-"+appID+" not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while getting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	/*@PostMapping("/accept/{appID}")
	public ResponseEntity<Object> acceptApplication(@PathVariable String appID, @RequestBody ApplicationDtlModel model ) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("Accept application {} | Input recieved - {}", appID, model);
			model.setAppId(appID);
			result = this.appService.updateStatus(model, ApplicationStatus.INPROGRESS.value());
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while accepting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Application-"+model.getAppId()+" not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while accepting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}*/
	
	@PostMapping("/reject/{appID}")
	public ResponseEntity<Object> rejectApplication(@PathVariable String appID, @RequestBody ApplicationDtlModel model ) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("Reject application {} | Input recieved - {}",appID, model);
			model.setAppId(appID);
			result = this.appService.updateStatus(model, ApplicationStatus.REJECTED.value());
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while rejecting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Application-"+model.getAppId()+" not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while rejecting application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
	
	@PostMapping("/award/{appID}")
	public ResponseEntity<Object> awardApplication(@PathVariable String appID, @RequestBody ApplicationDtlModel model ) {
		boolean result = false;
		ResponseEntity<Object> response = null;
		ResponseDetails respDtls = null;
		
		try {
			log.info("Award application {} | Input recieved - {}", appID, model);
			model.setAppId(appID);
			result = this.appService.updateStatus(model, ApplicationStatus.AWARDED.value());
			response = new ResponseEntity<Object>(result, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			log.error("Exception while awarding application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.NOT_FOUND.value()), InternalConstant.FAILED, "Application-"+model.getAppId()+" not found");
			response = new ResponseEntity<Object>(respDtls, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Exception while awarding application details->",e);
			respDtls = new ResponseDetails(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), InternalConstant.FAILED, e.getMessage());
			response = new ResponseEntity<Object>(respDtls, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
}
