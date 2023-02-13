package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.enums.ErrorCode;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Form;
import com.protean.beckn.api.model.common.Order;
import com.protean.beckn.api.model.common.State;
import com.protean.beckn.api.model.common.XInput;
import com.protean.beckn.api.model.common.XInputRequired;
import com.protean.beckn.api.model.confirm.ConfirmMessage;
import com.protean.beckn.api.model.confirm.ConfirmRequest;
import com.protean.beckn.api.model.onconfirm.OnConfirmMessage;
import com.protean.beckn.api.model.onconfirm.OnConfirmRequest;
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.exception.InvalidXInputSubmissionIDException;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.service.SchemeService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OnConfirmBuilder {

	@Autowired
	private ResponseBuilder responseBuilder;
	
	@Autowired
	SchemeService schemeService;
	
	@Autowired
	ApplicationService appService;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Value("${beckn.seller.id}")
	private String bppId;

	public OnConfirmRequest buildOnConfirm(ConfirmRequest request) {
		OnConfirmRequest replyModel = new OnConfirmRequest();

		Context context = this.responseBuilder.buildContext(request.getContext(), ContextAction.ON_CONFIRM.value());
		context.setBppId(this.bppId);

		OnConfirmMessage message = new OnConfirmMessage();

		try {
			message.setOrder(buildOrder(request.getContext().getTransactionId(),request.getMessage()));
			replyModel.setMessage(message);
		} catch (EntityNotFoundException e) {
			Error error = new Error();
			error.setCode(ErrorCode.ORDER_NOT_FOUND.value());
			error.setMessage("Order not found - "+e.getMessage());
			replyModel.setError(error);
		} catch (InvalidXInputSubmissionIDException e) {
			Error error = new Error();
			error.setCode(ErrorCode.INVALID_REQUEST_ERROR.value());
			error.setMessage("Invalid request - "+e.getMessage());
			replyModel.setError(error);
		} catch (Exception e) {
			Error error = new Error();
			error.setCode(ErrorCode.BUSINESS_ERROR.value());
			error.setMessage("Unable to confirm application request.");
			replyModel.setError(error);
		}

		context.setTimestamp(String.valueOf(new Timestamp(System.currentTimeMillis())));
		replyModel.setContext(context);
		
		return replyModel;
	}

	private Order buildOrder(String txnID, ConfirmMessage confirmMsg) throws Exception {
		log.info("Recieved confirm application details: {}", confirmMsg);
		Order order = null;
		
		try {
			//ApplicationDtlModel model = appService.getDetailsByAppID(confirmMsg.getOrder().getId());
			ApplicationDtlModel model = appService.getDetailsByTxnID(txnID);
			if(model == null) {
				throw new EntityNotFoundException(txnID);
			}

			SchemeModel scheme = schemeService.getDetailsBySchemeID(confirmMsg.getOrder().getProvider().getItems().get(0).getId());
			if(scheme.isAddtnlInfoReq()) {
				if(confirmMsg.getOrder().getProvider().getItems().get(0).getXinputRequired() != null 
						&& confirmMsg.getOrder().getProvider().getItems().get(0).getXinputRequired().getXinput().getForm() != null ) {
					String addInfoSubmsnID = confirmMsg.getOrder().getProvider().getItems().get(0).getXinputRequired().getXinput().getForm().getSubmissionId();
					log.info("Additional Info Submission ID: {}",addInfoSubmsnID);
					if(addInfoSubmsnID == null || addInfoSubmsnID.isEmpty() || !addInfoSubmsnID.trim().equalsIgnoreCase(model.getAddtnlInfoSubmsnId())) {
						model.setAppStatus(ApplicationStatus.REJECTED.value());
						model.setRemarks("Application rejected as additional info is not provided.");
					}else {
						model.setAppStatus(ApplicationStatus.INPROGRESS.value());
						model.setRemarks("Application accepted.");
					}
				}else {
					model.setAppStatus(ApplicationStatus.REJECTED.value());
					model.setRemarks("Application rejected as additional info is not provided.");
				}
				
			}else {
				model.setAppStatus(ApplicationStatus.INPROGRESS.value());
				model.setRemarks("Application accepted.");
			}
			model.setSchemeId(scheme.getId().toString());
			model.setSchemeProviderId(scheme.getSchemeProviderID());
			model.setApplcntId(confirmMsg.getOrder().getProvider().getFulfillments().get(0).getCustomer().getPerson().getId());
			model.setApplcntDtls(confirmMsg.getOrder().getProvider().getFulfillments().get(0).getCustomer());
			model.setDsepTxnId(txnID);
			
			ApplicationDtlModel appModel = appService.confirmApplication(model);
			if(appModel != null) {
				order = confirmMsg.getOrder();
				order.setId(appModel.getAppId());
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
					xinForm.setSubmissionId(appModel.getAddtnlInfoSubmsnId());
					xinput.setForm(xinForm);
					XInputRequired xinReq = new XInputRequired();
					xinReq.setXinput(xinput);
					order.getProvider().getItems().get(0).setXinputRequired(xinReq);
				}
				
			} else {
				throw new Exception("Exception occured while confirming order.");
			}
			
			
		} catch (Exception e) {
			log.error("Exception occurred while creating CONFIRM order - ",e);
			throw e;
		}
		
		return order;
	}

}