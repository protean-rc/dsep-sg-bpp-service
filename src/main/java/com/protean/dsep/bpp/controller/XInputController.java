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
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.util.JsonUtil;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import static com.protean.dsep.bpp.constant.ApplicationConstant.EXTERNAL_CONTEXT_ROOT;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityNotFoundException;

@RestController
@Slf4j
@RequestMapping(EXTERNAL_CONTEXT_ROOT)
public class XInputController {

	@Autowired
	ApplicationService appService;

	@Autowired
	JsonUtil jsonUtil;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;
	
	@GetMapping(value="/getForm/{appID}/{addInfoID}", produces=MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> getForm(@PathVariable String appID, @PathVariable String addInfoID, HttpResponse response) {
		log.info("Getting details of form for AppID-{}, AddtnlInfoID-{}", appID, addInfoID);
		String xinReq = null;
		HttpStatus responseStatus = HttpStatus.OK;
		try {
			String nonceVal = appService.getAppAddtnlInfoNonceValue(appID, addInfoID);
			xinReq = "<form action=\""+sellerUrl+"/submitApplicationDetails\" method=\"post\">\r\n"
					+ "     <label>Is your parents working with XYZ Limited?</label>\r\n"
					+ "     <input type=\"radio\" id=\"isFamMemWrkng_yes\" name=\"isFamMemWrkng\" value=\"Y\">\r\n"
					+ "     <label for=\"isFamMemWrkng_yes\">Yes</label>\r\n"
					+ "     <input type=\"radio\" id=\"isFamMemWrkng_no\" name=\"isFamMemWrkng\" value=\"N\">\r\n"
					+ "     <label for=\"isFamMemWrkng_no\">No</label><br />\r\n"
					+ "     <!-- If Yes, below inputs (Person Name, Department Name, Branch, ID Card, Relationship with Person) are required -->\r\n"
					+ "     <label for=\"pname\">Name of Person</label>\r\n"
					+ "     <input type=\"text\" id=\"pname\" name=\"pname\" /><br />\r\n"
					+ "     <label for=\"pdept\">Departmane Name</label>\r\n"
					+ "     <input type=\"text\" id=\"pdept\" name=\"pdept\" /><br />\r\n"
					+ "     <label for=\"pbranch\">Select Branch</label>\r\n"
					+ "     <select name=\"pbranch\" id=\"pbranch\">\r\n"
					+ "         <option value=\"mum\">Mumbai</option>\r\n"
					+ "         <option value=\"pne\">Pune</option>\r\n"
					+ "         <option value=\"bng\">Banglore</option>\r\n"
					+ "         <option value=\"del\">Delhi</option>\r\n"
					+ "     </select><br />\r\n"
					+ "     <label>Relationship with Person</label>\r\n"
					+ "     <input type=\"radio\" id=\"prel_m\" name=\"prel\" value=\"M\">\r\n"
					+ "     <label for=\"prel_m\">Mother</label>\r\n"
					+ "     <input type=\"radio\" id=\"prel_f\" name=\"prel\" value=\"F\">\r\n"
					+ "     <label for=\"prel_f\">Father</label><br />\r\n"
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
	
	@PostMapping(value="/submitApplicationDetails", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> submitApplicationDetails(@RequestBody MultiValueMap<String, String> formData){
		log.info("App Details:{}",formData);
		String response = null;
		Map<String,String> addtnlInfoMap = null;
		String nonce = null;
		HttpStatus responseStatus = HttpStatus.OK;
		int invalidFieldCount = 0;
		
		try {
			if(formData != null && formData.containsKey("nonce") && !formData.get("nonce").isEmpty()) {
				nonce = formData.get("nonce").get(0).trim();
				addtnlInfoMap = new HashMap<String, String>();
				if(formData.containsKey("isFamMemWrkng") && !formData.get("isFamMemWrkng").isEmpty() && 
						(formData.get("isFamMemWrkng").get(0).trim().equalsIgnoreCase("Y") || formData.get("isFamMemWrkng").get(0).trim().equalsIgnoreCase("N") )) {
					addtnlInfoMap.put("isFamMemWrkng", formData.get("isFamMemWrkng").get(0).trim());
					if(addtnlInfoMap.get("isFamMemWrkng").equalsIgnoreCase("Y")) {
						if((formData.containsKey("pname") && !formData.get("pname").isEmpty())
								&& (formData.containsKey("pdept") && !formData.get("pdept").isEmpty())
								&& (formData.containsKey("pbranch") && !formData.get("pbranch").isEmpty())
								&& (formData.containsKey("prel") && !formData.get("prel").isEmpty())){
							
							addtnlInfoMap.put("pname", formData.get("pname").get(0).trim());
							addtnlInfoMap.put("pdept", formData.get("pdept").get(0).trim());
							addtnlInfoMap.put("pbranch", formData.get("pbranch").get(0).trim());
							addtnlInfoMap.put("prel", formData.get("prel").get(0).trim());
						}else {
							invalidFieldCount++;
						}
					}
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
				String addInfoData = jsonUtil.toJson(addtnlInfoMap);
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
}
