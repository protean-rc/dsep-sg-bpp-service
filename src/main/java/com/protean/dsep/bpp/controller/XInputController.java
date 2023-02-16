package com.protean.dsep.bpp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.protean.dsep.bpp.exception.InvalidNonceValueException;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.XInputDataModel;
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import static com.protean.dsep.bpp.constant.ApplicationConstant.EXTERNAL_CONTEXT_ROOT;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

@RestController
@Slf4j
@RequestMapping(EXTERNAL_CONTEXT_ROOT)
public class XInputController {

	@Autowired
	ApplicationService appService;

	@Autowired
	JsonUtil jsonUtil;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;
	
	@GetMapping(value="/getForm/{txnID}/{addInfoID}", produces=MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> getForm(@PathVariable String txnID, @PathVariable String addInfoID, HttpResponse response) {
		log.info("Getting details of form for TxnID-{}, AddtnlInfoID-{}", txnID, addInfoID);
		String xinReq = null;
		HttpStatus responseStatus = HttpStatus.OK;
		try {
			String nonceVal = appService.getAppAddtnlInfoNonceValue(txnID, addInfoID);
			
			xinReq = "<form action=\""+sellerUrl+"/submitApplicationDetails\" method=\"post\">\r\n"
					+ "     <label for=\"name\">Name</label>\r\n"
					+ "     <input type=\"text\" id=\"name\" name=\"name\" /><br />\r\n"
					+ "     <label for=\"address\">Address</label>\r\n"
					+ "     <input type=\"text\" id=\"address\" name=\"address\" /><br />\r\n"
					+ "     <label for=\"phone\">Phone No.</label>\r\n"
					+ "     <input type=\"text\" id=\"phone\" name=\"phone\" /><br />\r\n"
					+ "     <label for=\"need-of-scholarship\">Tell us why you need scholarship</label>\r\n"
					+ "     <textarea id=\"need-of-scholarship\" name=\"need-of-scholarship\" /></textarea><br />\r\n"
					+ "     <label for=\"docs\">Document URL</label>\r\n"
					+ "     <input type=\"url\" id=\"docs\" name=\"docs\" />\r\n"
					+ "\r\n"
					+ "     <br /><br />\r\n"
					+ "\r\n"
					+ "     <input type=\"hidden\" id=\"nonce\" name=\"nonce\" value=\""+nonceVal+"\" />\r\n"
					+ " </form>";
		} catch (EntityNotFoundException e) {
			log.error(e.getMessage());
			responseStatus = HttpStatus.NOT_FOUND;
		} catch (Exception e) {
			log.error("Exception occured while getting addtional info form - ",e);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		return ResponseEntity.status(responseStatus).body(xinReq);
	}
	
//	@PostMapping(value="/submitApplicationDetails", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//	public ResponseEntity<String> submitApplicationDetails(@RequestBody MultiValueMap<String, String> formData){
//		log.info("App Details:{}",formData);
//		String response = null;
//		Map<String,String> addtnlInfoMap = null;
//		String nonce = null;
//		HttpStatus responseStatus = HttpStatus.OK;
//		int invalidFieldCount = 0;
//		
//		try {
//			if(formData != null && formData.containsKey("nonce") && !formData.get("nonce").isEmpty()) {
//				nonce = formData.get("nonce").get(0).trim();
//				addtnlInfoMap = new HashMap<String, String>();
//				if(formData.containsKey("isFamMemWrkng") && !formData.get("isFamMemWrkng").isEmpty() && 
//						(formData.get("isFamMemWrkng").get(0).trim().equalsIgnoreCase("Y") || formData.get("isFamMemWrkng").get(0).trim().equalsIgnoreCase("N") )) {
//					addtnlInfoMap.put("isFamMemWrkng", formData.get("isFamMemWrkng").get(0).trim());
//					if(addtnlInfoMap.get("isFamMemWrkng").equalsIgnoreCase("Y")) {
//						if((formData.containsKey("pname") && !formData.get("pname").isEmpty())
//								&& (formData.containsKey("pdept") && !formData.get("pdept").isEmpty())
//								&& (formData.containsKey("pbranch") && !formData.get("pbranch").isEmpty())
//								&& (formData.containsKey("prel") && !formData.get("prel").isEmpty())){
//							
//							addtnlInfoMap.put("pname", formData.get("pname").get(0).trim());
//							addtnlInfoMap.put("pdept", formData.get("pdept").get(0).trim());
//							addtnlInfoMap.put("pbranch", formData.get("pbranch").get(0).trim());
//							addtnlInfoMap.put("prel", formData.get("prel").get(0).trim());
//						}else {
//							invalidFieldCount++;
//						}
//					}
//				}else {
//					invalidFieldCount++;
//				}
//			}else {
//				invalidFieldCount++;
//			}
//			
//			if(invalidFieldCount > 0) {
//				responseStatus = HttpStatus.BAD_REQUEST;
//				response = "Invalid form input fields";
//			}else {
//				ApplicationDtlModel model = appService.getApplicationDtlByXinputNonceVal(nonce);
//				if(model == null) {
//					throw new InvalidNonceValueException("Invalid nonce value-"+nonce);
//				}
//				responseStatus = HttpStatus.OK;
//				String addInfoData = jsonUtil.toJson(addtnlInfoMap);
//				model.setAddtnlDtls(addInfoData);
//				String submsnID = appService.saveAppAddtnlInfo(model);
//				response = submsnID;
//			}
//		} catch (InvalidNonceValueException e) {
//			log.error(e.getMessage());
//			responseStatus = HttpStatus.BAD_REQUEST;
//		} catch (Exception e) {
//			log.error("Exception occured while submitting addtional info form - ",e);
//			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//		}
//		return ResponseEntity.status(responseStatus).body(response);
//		
//	}
	
	@PostMapping(value="/submitApplicationDetails", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> submitApplicationDetails(@RequestBody MultiValueMap<String, String> formData){
		log.info("App Details:{}",formData);
		String response = null;
		Map<String,String> addtnlInfoMap = null;
		XInputDataModel addtnlInfoData = null;
		String nonce = null;
		HttpStatus responseStatus = HttpStatus.OK;
		int invalidFieldCount = 0;
		
		try {
			if(formData != null && formData.containsKey("nonce") && !formData.get("nonce").isEmpty()) {
				nonce = formData.get("nonce").get(0).trim();
				addtnlInfoMap = new HashMap<String, String>();
				
				if((formData.containsKey("name") && !formData.get("name").isEmpty())
						&& (formData.containsKey("phone") && !formData.get("phone").isEmpty())
						&& (formData.containsKey("address") && !formData.get("address").isEmpty())
						&& (formData.containsKey("need-of-scholarship") && !formData.get("need-of-scholarship").isEmpty())
						&& (formData.containsKey("docs") && !formData.get("docs").isEmpty())){
					
					addtnlInfoData = new XInputDataModel();
//					addtnlInfoMap.put("name", formData.get("name").get(0).trim());
//					addtnlInfoMap.put("phone", formData.get("phone").get(0).trim());
//					addtnlInfoMap.put("address", formData.get("address").get(0).trim());
//					addtnlInfoMap.put("need-of-scholarship", formData.get("need-of-scholarship").get(0).trim());
//					addtnlInfoMap.put("docs", formData.get("docs").get(0).trim());
					addtnlInfoData.setName(formData.get("name").get(0).trim());
					addtnlInfoData.setPhone(formData.get("phone").get(0).trim());
					addtnlInfoData.setAddress(formData.get("address").get(0).trim());
					addtnlInfoData.setNeedOfScholarship(formData.get("need-of-scholarship").get(0).trim());
					addtnlInfoData.setDocUrl(formData.get("docs").get(0).trim());
					
				}else {
					invalidFieldCount++;
				}
			}else {
				invalidFieldCount++;
			}
			
			if(invalidFieldCount > 0) {
				responseStatus = HttpStatus.BAD_REQUEST;
				response = "Invalid form input fields";
			}else {
				ApplicationDtlModel model = appService.getApplicationDtlByXinputNonceVal(nonce);
				if(model == null) {
					throw new InvalidNonceValueException("Invalid nonce value-"+nonce);
				}
				responseStatus = HttpStatus.OK;
				//String addInfoData = jsonUtil.toJson(addtnlInfoMap);
				String addInfoData = jsonUtil.toJson(addtnlInfoData);
				model.setAddtnlDtls(addInfoData);
				String submsnID = appService.saveAppAddtnlInfo(model);
				response = submsnID;
			}
		} catch (InvalidNonceValueException e) {
			log.error(e.getMessage());
			responseStatus = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			log.error("Exception occured while submitting addtional info form - ",e);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(responseStatus).body(response);
		
	}
	
	@PostMapping(value="/submitXInputDetails")
	public ResponseEntity<String> submitXInputDetails(@RequestBody XInputDataModel xinputData){
		log.info("XInputDetails=>{}",xinputData);
		String response = null;
		HttpStatus responseStatus = HttpStatus.OK;
		int invalidFieldCount = 0;
		
		try {
			if(xinputData != null && xinputData.getName() != null && !xinputData.getName().isEmpty()
					&& xinputData.getPhone() != null && !xinputData.getPhone().isEmpty()
					&& xinputData.getAddress() != null && !xinputData.getAddress().isEmpty()
					&& xinputData.getNeedOfScholarship() != null && !xinputData.getNeedOfScholarship().isEmpty()
					&& xinputData.getDocUrl() != null && !xinputData.getDocUrl().isEmpty()) {
				
				response = UUID.randomUUID().toString();
			}else {
				invalidFieldCount++;
			}
			
			if(invalidFieldCount > 0) {
				responseStatus = HttpStatus.BAD_REQUEST;
				response = "Invalid form input fields. Please enter all details.";
			}
		} catch (Exception e) {
			log.error("Exception occured while submitting xinput data - ",e);
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(responseStatus).body(response);
		
	} 
}
