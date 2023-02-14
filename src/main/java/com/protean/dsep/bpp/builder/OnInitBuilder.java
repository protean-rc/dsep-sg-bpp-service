package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;

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
import com.protean.beckn.api.model.common.State;
import com.protean.beckn.api.model.common.XInput;
import com.protean.beckn.api.model.common.XInputRequired;
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
			error.setMessage("Unable to init application request.");
			replyModel.setError(error);
		}

		context.setTimestamp(commonUtil.getDateTimeString());
		replyModel.setContext(context);
		
		return replyModel;
	}
	
	private Order buildOrder(String txnID, InitMessage initMsg) throws Exception {
		log.info("Recieved init application details: {}", initMsg);
		Order order = null;
		
		try {
			ApplicationDtlModel model = new ApplicationDtlModel();
			SchemeModel scheme = schemeService.getDetailsBySchemeID(initMsg.getOrder().getProvider().getItems().get(0).getId());
			model.setSchemeProviderId(initMsg.getOrder().getProvider().getId());
			model.setSchemeId(scheme.getId().toString());
			model.setApplcntId(initMsg.getOrder().getProvider().getFulfillments().get(0).getCustomer().getPerson().getId());
			model.setApplcntDtls(initMsg.getOrder().getProvider().getFulfillments().get(0).getCustomer());
			model.setDsepTxnId(txnID);
			model.setCreatedBy(initMsg.getOrder().getProvider().getFulfillments().get(0).getCustomer().getPerson().getId());
			
			ApplicationDtlModel appModel = appService.initApplication(model);
			if(appModel != null) {
				order = initMsg.getOrder();
				//order.setId(appModel.getAppId());
				
				State appState = new State();
				Descriptor appStatusDesc = new Descriptor();
				appStatusDesc.setCode(ApplicationStatus.APPSTATUS.get(appModel.getAppStatus()));
				appStatusDesc.setShortDesc(appModel.getRemarks());
				appState.setUpdatedAt(appModel.getUpdatedAt());
				appState.setUpdatedBy(appModel.getUpdatedBy());
				appState.setDescriptor(appStatusDesc);
				order.getProvider().getFulfillments().get(0).setState(appState);

				if(scheme.isAddtnlInfoReq()) {
					XInput xinput = new XInput();
					Form xinForm = new Form();
					xinForm.setUrl(xinUrl.concat(InternalConstant.FW_SLASH).concat(appModel.getAppId())
							.concat(InternalConstant.FW_SLASH).concat(appModel.getAddtnlInfoId()));
					xinForm.setMimeType(MimeTypeUtils.TEXT_HTML_VALUE);
					xinput.setForm(xinForm);
					XInputRequired xinReq = new XInputRequired();
					xinReq.setXinput(xinput);
					order.getProvider().getItems().get(0).setXinputRequired(xinReq);
				}
				
			} else {
				throw new Exception("Empty order details received from application service.");
			}
			
			
		} catch (Exception e) {
			log.error("Exception occurred while creating INIT order - ",e);
			throw e;
		}
		
		return order;
	}

}