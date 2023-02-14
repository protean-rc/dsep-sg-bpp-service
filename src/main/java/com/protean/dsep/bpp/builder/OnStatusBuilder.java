package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.enums.ErrorCode;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Fulfillment;
import com.protean.beckn.api.model.common.Item;
import com.protean.beckn.api.model.common.Order;
import com.protean.beckn.api.model.common.Provider;
import com.protean.beckn.api.model.common.State;
import com.protean.beckn.api.model.onstatus.OnStatusMessage;
import com.protean.beckn.api.model.onstatus.OnStatusRequest;
import com.protean.beckn.api.model.status.StatusMessage;
import com.protean.beckn.api.model.status.StatusRequest;
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.exception.InvalidXInputSubmissionIDException;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.model.SchemeProviderModel;
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.service.SchemeProviderService;
import com.protean.dsep.bpp.service.SchemeService;
import com.protean.dsep.bpp.util.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OnStatusBuilder {

	@Autowired
	private ResponseBuilder responseBuilder;
	
	@Autowired
	SchemeService schemeService;
	
	@Autowired
	SchemeProviderService schemeProviderService;
	
	@Autowired
	ApplicationService appService;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Value("${beckn.seller.id}")
	private String bppId;

	public OnStatusRequest buildOnStatus(StatusRequest request) {
		OnStatusRequest replyModel = new OnStatusRequest();

		Context context = this.responseBuilder.buildContext(request.getContext(), ContextAction.ON_STATUS.value());
		context.setBppId(this.bppId);

		OnStatusMessage message = new OnStatusMessage();

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
			error.setMessage("Unable to get status of application.");
			replyModel.setError(error);
		}

		context.setTimestamp(commonUtil.getDateTimeString());
		replyModel.setContext(context);
		
		return replyModel;
	}

	private Order buildOrder(String txnID, StatusMessage statusMsg) throws Exception {
		log.info("Recieved request to get application status: {}", statusMsg);
		Order order = null;
		
		try {
			ApplicationDtlModel model = appService.getDetailsByAppID(statusMsg.getOrderId());
			if(model == null) {
				throw new EntityNotFoundException(statusMsg.getOrderId());
			}else {
				order = new Order();
				order.setId(model.getAppId());
				
				SchemeProviderModel schemeProvider = schemeProviderService.getDetailsBySchemeProviderID(model.getSchemeProviderId());
				Provider provider = new Provider();
				Descriptor providerDescriptor = new Descriptor();
				providerDescriptor.setName(schemeProvider.getSchemeProviderName());
				provider.setId(model.getSchemeProviderId());
				provider.setDescriptor(providerDescriptor);
				
				SchemeModel scheme = schemeService.getDetailsByID(model.getSchemeId());
				List<Item> itemList = new ArrayList<Item>();
				Item item = new Item();
				Descriptor itemDescriptor = new Descriptor();
				itemDescriptor.setName(scheme.getSchemeName());
				item.setId(scheme.getSchemeID());
				item.setDescriptor(itemDescriptor);
				itemList.add(item);
				provider.setItems(itemList);
				
				List<Fulfillment> flist = new ArrayList<Fulfillment>();
				Fulfillment fulmnt = new Fulfillment();
				State appState = new State();
				Descriptor appStatusDesc = new Descriptor();
				appStatusDesc.setCode(ApplicationStatus.APPSTATUS.get(model.getAppStatus()));
				appStatusDesc.setShortDesc(model.getRemarks());
				appState.setUpdatedAt(model.getUpdatedAt());
				appState.setUpdatedBy(model.getUpdatedBy());
				appState.setDescriptor(appStatusDesc);
				fulmnt.setState(appState);
				flist.add(fulmnt);
				provider.setFulfillments(flist);
				
				order.setProvider(provider);
			}
			
			
		} catch (Exception e) {
			log.error("Exception occurred while creating CONFIRM order - ",e);
			throw e;
		}
		
		return order;
	}

}