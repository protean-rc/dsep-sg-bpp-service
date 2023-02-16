package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Form;
import com.protean.beckn.api.model.common.Order;
import com.protean.beckn.api.model.common.OrderState;
import com.protean.beckn.api.model.common.XInput;
import com.protean.beckn.api.model.init.InitMessage;
import com.protean.beckn.api.model.init.InitRequest;
import com.protean.beckn.api.model.oninit.OnInitMessage;
import com.protean.beckn.api.model.oninit.OnInitRequest;
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.service.SchemeService;
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OnInitBuilder {

	@Autowired
	private ResponseBuilder responseBuilder;
	
	@Autowired
	private ApplicationService appService;
	
	@Autowired
	private SchemeService schemeService;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	JsonUtil jsonUtil;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Value("${beckn.seller.id}")
	private String bppId;
	
	@Value("${xinput.form.base_url}")
	private String xinUrl;

	public OnInitRequest buildOnInit(InitRequest request) {
		OnInitRequest replyModel = new OnInitRequest();

		Context context = this.responseBuilder.buildContext(request.getContext(), ContextAction.ON_INIT.value());
		context.setBppId(this.bppId);

		OnInitMessage message = new OnInitMessage();

		try {
			message.setOrder(buildOrder(request.getContext().getTransactionId(),request.getMessage()));
			replyModel.setMessage(message);
		} catch (Exception e) {
			Error error = new Error();
			error.setCode("40000");
			error.setMessage("Unable to init application request - "+e.getMessage());
			replyModel.setError(error);
		}

		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
		replyModel.setContext(context);
		
		return replyModel;
	}
	
	private Order buildOrder(String txnID, InitMessage initMsg) throws Exception {
		log.info("Recieved init application details: {}", initMsg);
		Order order = null;
		
		try {
			order = initMsg.getOrder();
			
			SchemeModel scheme = schemeService.getDetailsBySchemeID(initMsg.getOrder().getItems().get(0).getId());
			
			if(scheme.isAddtnlInfoReq()) {
				if(initMsg.getOrder().getItems().get(0).getXinput() != null 
						&& initMsg.getOrder().getItems().get(0).getXinput().getForm() != null ) {
					Object addtnlDtls = initMsg.getOrder().getItems().get(0).getXinput().getForm().getData();
					String addInfoSubmsnID = initMsg.getOrder().getItems().get(0).getXinput().getForm().getSubmissionId();
					log.info("XInput Additional Details: {}",addtnlDtls);
					
					if (addtnlDtls == null && (addInfoSubmsnID != null && !addInfoSubmsnID.isEmpty())) {
						ApplicationDtlModel model = appService.getDetailsByTxnID(txnID);
						if(model.getAddtnlInfoSubmsnId().equalsIgnoreCase(addInfoSubmsnID)) {
							order.getItems().get(0).getXinput().getForm().setData(model.getAddtnlDtls());
						}else {
							throw new Exception("Invalid additional info submission id");
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.error("Exception occurred while creating INIT order - ",e);
			throw e;
		}
		
		return order;
	}
}