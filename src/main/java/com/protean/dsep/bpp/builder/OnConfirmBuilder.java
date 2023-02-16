package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.enums.ErrorCode;
import com.protean.beckn.api.enums.OrderStatus;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Form;
import com.protean.beckn.api.model.common.Order;
import com.protean.beckn.api.model.common.OrderState;
import com.protean.beckn.api.model.common.XInput;
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
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;

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
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	JsonUtil jsonUtil;
	
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

		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
		replyModel.setContext(context);
		
		return replyModel;
	}

	private Order buildOrder(String txnID, ConfirmMessage confirmMsg) throws Exception {
		log.info("Recieved confirm application details: {}", confirmMsg);
		Order order = null;
		
		try {
			
			/*ApplicationDtlModel model = new ApplicationDtlModel();
			SchemeModel scheme = schemeService.getDetailsBySchemeID(confirmMsg.getOrder().getItems().get(0).getId());
			model.setSchemeProviderId(confirmMsg.getOrder().getProvider().getId());
			model.setSchemeId(scheme.getId().toString());
			model.setApplcntId(confirmMsg.getOrder().getFulfillments().get(0).getCustomer().getPerson().getId());
			model.setApplcntDtls(confirmMsg.getOrder());
			model.setDsepTxnId(txnID);
			model.setCreatedBy(confirmMsg.getOrder().getFulfillments().get(0).getCustomer().getPerson().getId());*/
			
			ApplicationDtlModel model = appService.getDetailsByTxnID(txnID);
			if(model == null) {
				throw new EntityNotFoundException(txnID);
			}
			
			model.setApplcntId(confirmMsg.getOrder().getFulfillments().get(0).getCustomer().getPerson().getId());
			model.setApplcntDtls(confirmMsg.getOrder().getFulfillments().get(0).getCustomer());
			model.setDsepTxnId(txnID);
			model.setCreatedBy(confirmMsg.getOrder().getFulfillments().get(0).getCustomer().getPerson().getId());
			
			SchemeModel scheme = schemeService.getDetailsBySchemeID(confirmMsg.getOrder().getItems().get(0).getId());
			
			if(scheme.isAddtnlInfoReq()) {
				if(confirmMsg.getOrder().getItems().get(0).getXinput() != null 
						&& confirmMsg.getOrder().getItems().get(0).getXinput().getForm() != null ) {
					Object addtnlDtls = confirmMsg.getOrder().getItems().get(0).getXinput().getForm().getData();
					String addInfoSubmsnID = confirmMsg.getOrder().getItems().get(0).getXinput().getForm().getSubmissionId();
					log.info("XInput Additional Details: {}",addtnlDtls);
					
					if (addtnlDtls != null){
						model.setAddtnlDtls(jsonUtil.toJson(addtnlDtls));
						model.setAddtnlInfoSubmsnId(addInfoSubmsnID);
						model.setAppStatus(ApplicationStatus.INPROGRESS.value());
						model.setRemarks("Application accepted.");
					}else if (addtnlDtls == null && (addInfoSubmsnID != null && !addInfoSubmsnID.isEmpty())) {
						if(addInfoSubmsnID.trim().equalsIgnoreCase(model.getAddtnlInfoSubmsnId())) {
							model.setAppStatus(ApplicationStatus.INPROGRESS.value());
							model.setRemarks("Application accepted.");
						}else {
							model.setAppStatus(ApplicationStatus.REJECTED.value());
							model.setRemarks("Application rejected as additional info submission id is invalid");
						}						
					}else {
						model.setAppStatus(ApplicationStatus.REJECTED.value());
						model.setRemarks("Application rejected as additional info is not provided.");
					}
				}else {
					model.setAppStatus(ApplicationStatus.REJECTED.value());
					model.setRemarks("Application rejected as additional info is not provided.");
				}
				
			}else {
				model.setAppStatus(ApplicationStatus.INPROGRESS.value());
				model.setRemarks("Application accepted.");
			}
			
			ApplicationDtlModel appModel = appService.confirmApplication(model);
			//ApplicationDtlModel appModel = appService.initApplication(model);
			
			if(appModel != null) {
				order = confirmMsg.getOrder();
				order.setId(appModel.getAppId());
				if(appModel.getAppStatus() == ApplicationStatus.REJECTED.value()) {
					order.setStatus(OrderStatus.CANCELLED.value());
				}else {
					order.setStatus(OrderStatus.ACTIVE.value());
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
	
	/*
	private Order buildOrder(String txnID, ConfirmMessage confirmMsg) throws Exception {
		log.info("Recieved confirm application details: {}", confirmMsg);
		Order order = null;
		
		try {
			ApplicationDtlModel model = appService.getDetailsByTxnID(txnID);
			if(model == null) {
				throw new EntityNotFoundException(txnID);
			}

			SchemeModel scheme = schemeService.getDetailsBySchemeID(confirmMsg.getOrder().getProvider().getItems().get(0).getId());
			if(scheme.isAddtnlInfoReq()) {
				if(confirmMsg.getOrder().getProvider().getItems().get(0).getXinput() != null 
						&& confirmMsg.getOrder().getProvider().getItems().get(0).getXinput().getForm() != null ) {
					String addInfoSubmsnID = confirmMsg.getOrder().getProvider().getItems().get(0).getXinput().getForm().getSubmissionId();
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
				OrderState appState = new OrderState();
				Descriptor appStatusDesc = new Descriptor();
				appState.setCode(String.valueOf(appModel.getAppStatus()));
				appState.setName(ApplicationStatus.APPSTATUS.get(appModel.getAppStatus()));
				order.getProvider().getFulfillments().get(0).setState(appState);
				
				if(scheme.isAddtnlInfoReq()) {
					XInput xinput = new XInput();
					Form xinForm = new Form();
					xinForm.setSubmissionId(appModel.getAddtnlInfoSubmsnId());
					xinput.setForm(xinForm);
					order.getProvider().getItems().get(0).setXinput(xinput);
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
	*/
}