package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.enums.ErrorCode;
import com.protean.beckn.api.enums.OrderStatus;
import com.protean.beckn.api.model.common.Contact;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Customer;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Error;
import com.protean.beckn.api.model.common.Form;
import com.protean.beckn.api.model.common.Fulfillment;
import com.protean.beckn.api.model.common.Item;
import com.protean.beckn.api.model.common.Order;
import com.protean.beckn.api.model.common.Provider;
import com.protean.beckn.api.model.common.Stop;
import com.protean.beckn.api.model.common.Tag;
import com.protean.beckn.api.model.common.TagGroup;
import com.protean.beckn.api.model.common.Time;
import com.protean.beckn.api.model.common.XInput;
import com.protean.beckn.api.model.common.OrderState;
import com.protean.beckn.api.model.common.Person;
import com.protean.beckn.api.model.common.Price;
import com.protean.beckn.api.model.onstatus.OnStatusMessage;
import com.protean.beckn.api.model.onstatus.OnStatusRequest;
import com.protean.beckn.api.model.status.StatusMessage;
import com.protean.beckn.api.model.status.StatusRequest;
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.dao.SchemeCategoryRepo;
import com.protean.dsep.bpp.entity.DsepSchemeCategory;
import com.protean.dsep.bpp.exception.InvalidXInputSubmissionIDException;
import com.protean.dsep.bpp.model.AcademicDtlsModel;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.SchemeEligibilityModel;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.model.SchemeProviderModel;
import com.protean.dsep.bpp.model.XInputDataModel;
import com.protean.dsep.bpp.service.ApplicationService;
import com.protean.dsep.bpp.service.SchemeProviderService;
import com.protean.dsep.bpp.service.SchemeService;
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;

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
	
	@Autowired
	JsonUtil jsonUtil;
	
	@Autowired
	SchemeCategoryRepo schemeCatRepo;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Value("${beckn.seller.id}")
	private String bppId;

	@Value("${xinput.form.base_url}")
	private String xinUrl;
	
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

		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
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
				//OrderState appState = new OrderState();
				if(model.getAppStatus() == ApplicationStatus.REJECTED.value()) {
					order.setStatus(OrderStatus.CANCELLED.value());
				}else if (model.getAppStatus() == ApplicationStatus.DISBURSED.value() || model.getAppStatus() == ApplicationStatus.CLOSED.value()) {
					order.setStatus(OrderStatus.COMPLETE.value());
				} else {
					order.setStatus(OrderStatus.ACTIVE.value());
				}
				
				SchemeProviderModel provider = schemeProviderService.getDetailsBySchemeProviderID(model.getSchemeProviderId());
				
				SchemeModel scheme = schemeService.getDetailsByID(model.getSchemeId());
				
				//setting provider details
				Provider pr = new Provider();
				pr.setId(provider.getSchemeProviderId());
				pr.setDescriptor(new Descriptor());
				pr.getDescriptor().setName(provider.getSchemeProviderName());
				pr.getDescriptor().setShortDesc(provider.getSchemeProviderDescription());
				
				//setting fulfillment details
				List<Fulfillment> fulfillments = new ArrayList<>();
				
				Fulfillment fulfillment = new Fulfillment();
				fulfillment.setId(InternalConstant.FULFILLMENT_ID_PREFIX.concat(scheme.getSchemeID().split("_")[1]));
				fulfillment.setType(scheme.getSchemeType());
				
				List<Stop> stops = new ArrayList<Stop>();
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(InternalConstant.SCHEME_DATE_FORMAT);
				
				Stop start = new Stop();
				start.setType(InternalConstant.APPLICATION_START);
				Time startTime = new Time();
				startTime.setTimestamp(commonUtil.getDateTimeString(simpleDateFormat.parse(scheme.getStartDate())));
				start.setTime(startTime);
				
				Stop end = new Stop();
				end.setType(InternalConstant.APPLICATION_END);
				Time endTime = new Time();
				endTime.setTimestamp(commonUtil.getDateTimeString(simpleDateFormat.parse(scheme.getEndDate())));
				end.setTime(endTime);
				
				stops.add(start);
				stops.add(end);
				fulfillment.setStops(stops);
				
				Contact contact = new Contact();
				contact.setEmail(scheme.getSpocEmail());
				contact.setPhone(scheme.getHelpdeskNo());
				fulfillment.setContact(contact);
				
				
				
				//Setting Items
				List<Item> items = new ArrayList<>();
				
				Item item = new Item();
				item.setId(scheme.getSchemeID());
				item.setDescriptor(new Descriptor());
				item.getDescriptor().setName(scheme.getSchemeName());
				item.getDescriptor().setShortDesc(scheme.getSchemeDescription());
				
				DsepSchemeCategory schemeCat = schemeCatRepo.findByDscCatCode(scheme.getSchemeFor());
				List<String> catIds = new ArrayList<String>();
				catIds.add(InternalConstant.CATEGORY_ID_PREFIX.concat(String.valueOf(schemeCat.getDscCatId())));
				item.setCategoryIds(catIds);
				
				Price price = new Price();
				price.setCurrency("INR");
				price.setValue(String.valueOf(scheme.getSchemeAmount()));
				item.setPrice(price);

				List<TagGroup> tagData = new ArrayList<>();
				
				SchemeEligibilityModel eligibilityData = scheme.getEligibility();
				
				Customer customer = null;
				
				if(eligibilityData != null) {
										
					if(eligibilityData.getGender() != null && !eligibilityData.getGender().isBlank()) {
						customer = new Customer();
						Person person = new Person();
						person.setGender(eligibilityData.getGender().trim());
						customer.setPerson(person);
						fulfillment.setCustomer(customer);
					}
					
					List<AcademicDtlsModel> acadDtls =  eligibilityData.getAcadDtls();
					if(acadDtls != null && acadDtls.size()>0) {
						for (AcademicDtlsModel acad : acadDtls) {
							TagGroup tgAcad = new TagGroup();
							tgAcad.setDescriptor(new Descriptor());
							tgAcad.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_ACAD_QUAL_CODE);
							tgAcad.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_ACAD_QUAL_NAME);
							tgAcad.setDisplay(true);
							
							List<Tag> tagList = new ArrayList<Tag>();
							
							Tag t1 = new Tag();
							t1.setDescriptor(new Descriptor());
							t1.getDescriptor().setCode(acad.getCourseLevelID());
							t1.getDescriptor().setName(acad.getCourseLevelName());
							t1.setValue(acad.getCourseName());
							t1.setDisplay(true);
							tagList.add(t1);
							
							Tag t2 = new Tag();
							t2.setDescriptor(new Descriptor());
							t2.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_PERCTG_GRAD_CODE);
							t2.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_PERCTG_GRAD_NAME);
							t2.setValue(">= ".concat(acad.getScoreValue()));
							t2.setDisplay(true);
							tagList.add(t2);
							
							Tag t3 = new Tag();
							t3.setDescriptor(new Descriptor());
							t3.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_PASS_YR_CODE);
							t3.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_PASS_YR_NAME);
							t3.setValue(acad.getPassingYear());
							t3.setDisplay(false);
							tagList.add(t3);
							
							tgAcad.setList(tagList);
							
							tagData.add(tgAcad);
						}
					}
					
					
					if(eligibilityData.getCaste() != null && eligibilityData.getCaste().size() > 0) {
						TagGroup tdataCast = new TagGroup();
						tdataCast.setDescriptor(new Descriptor());
						tdataCast.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_CODE);
						tdataCast.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_NAME);
						tdataCast.setDisplay(true);
						
						List<Tag> tagDataCastList = new ArrayList<Tag>();
												
						for (String castData : eligibilityData.getCaste()) {
							Tag tagCast = new Tag();
							tagCast.setDescriptor(new Descriptor());
							tagCast.getDescriptor().setCode(castData.trim());
							tagCast.getDescriptor().setName(castData.trim());
							tagCast.setValue(castData.trim());
							tagCast.setDisplay(true);
							tagDataCastList.add(tagCast);
						}
						tdataCast.setList(tagDataCastList);
						tagData.add(tdataCast);
					}
					
					if(eligibilityData.getFamilyIncome() != null && !eligibilityData.getFamilyIncome().isBlank()) {
						TagGroup tdataFin = new TagGroup();
						tdataFin.setDescriptor(new Descriptor());
						tdataFin.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_CODE);
						tdataFin.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_NAME);
						tdataFin.setDisplay(true);
						
						List<Tag> tagDataFinList = new ArrayList<Tag>();
						
						Tag tagFin = new Tag();
						tagFin.setDescriptor(new Descriptor());
						tagFin.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_CODE);
						tagFin.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_NAME);
						tagFin.setValue("<= ".concat(eligibilityData.getFamilyIncome().trim()));
						tagFin.setDisplay(true);
						tagDataFinList.add(tagFin);
						
						tdataFin.setList(tagDataFinList);
						tagData.add(tdataFin);
					}

				}
				
				TagGroup tdataBenefit = new TagGroup();
				tdataBenefit.setDescriptor(new Descriptor());
				tdataBenefit.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_BENEFITS_CODE);
				tdataBenefit.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_BENEFITS_NAME);
				tdataBenefit.setDisplay(true);
				
				List<Tag> tagDataBenefitList = new ArrayList<Tag>();
										
				Tag tagSchemeAmt = new Tag();
				tagSchemeAmt.setDescriptor(new Descriptor());
				tagSchemeAmt.getDescriptor().setCode(InternalConstant.CATALOG_SCHEME_BENEFITS_AMT_CODE);
				tagSchemeAmt.getDescriptor().setName(InternalConstant.CATALOG_SCHEME_BENEFITS_AMT_NAME);
				tagSchemeAmt.setValue(scheme.getSchemeAmount() > 0 ? commonUtil.getSchemeAmountString(scheme.getSchemeAmount()):"0");
				tagSchemeAmt.setDisplay(true);
				tagDataBenefitList.add(tagSchemeAmt);
				
				tdataBenefit.setList(tagDataBenefitList);
				tagData.add(tdataBenefit);
				
				item.setTags(tagData);
				
				XInput xinput = new XInput();
				if(scheme.isAddtnlInfoReq()) {
					xinput.setRequired(true);
					Form xinForm = new Form();
					xinForm.setUrl(xinUrl.concat(InternalConstant.FW_SLASH).concat(txnID)
							.concat(InternalConstant.FW_SLASH).concat(model.getAddtnlInfoId()));
					xinForm.setMimeType(MimeTypeUtils.TEXT_HTML_VALUE);
					xinForm.setData(model.getAddtnlDtls() != null ? jsonUtil.toModel(model.getAddtnlDtls(), XInputDataModel.class) : null);
					xinput.setForm(xinForm);
				}else {
					xinput.setRequired(false);
				}
				item.setXinput(xinput);
				
				order.setProvider(pr);
				
				fulfillments.add(fulfillment);
				order.setFulfillments(fulfillments);
				
				items.add(item);
				order.setItems(items);
			}
			
			
		} catch (Exception e) {
			log.error("Exception occurred while getting order STATUS - ",e);
			throw e;
		}
		
		return order;
	}

}