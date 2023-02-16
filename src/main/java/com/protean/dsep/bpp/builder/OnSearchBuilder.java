package com.protean.dsep.bpp.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.protean.beckn.api.enums.ContextAction;
import com.protean.beckn.api.model.common.Catalog;
import com.protean.beckn.api.model.common.Category;
import com.protean.beckn.api.model.common.Contact;
import com.protean.beckn.api.model.common.Context;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.Fulfillment;
import com.protean.beckn.api.model.common.Item;
import com.protean.beckn.api.model.common.Price;
import com.protean.beckn.api.model.common.Provider;
import com.protean.beckn.api.model.common.Stop;
import com.protean.beckn.api.model.common.Tag;
import com.protean.beckn.api.model.common.TagGroup;
import com.protean.beckn.api.model.common.Time;
import com.protean.beckn.api.model.onsearch.OnSearchMessage;
import com.protean.beckn.api.model.onsearch.OnSearchRequest;
import com.protean.beckn.api.model.search.SearchMessage;
import com.protean.beckn.api.model.search.SearchRequest;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.dao.BppDao;
import com.protean.dsep.bpp.dao.SchemeCategoryRepo;
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;
import com.protean.dsep.bpp.entity.DsepScheme;
import com.protean.dsep.bpp.entity.DsepSchemeCategory;
import com.protean.dsep.bpp.entity.DsepSchemeProvider;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OnSearchBuilder {

	@Autowired
	private ResponseBuilder responseBuilder;

	@Autowired
	private BppDao dao;

	@Autowired
	private SchemeCategoryRepo schemeCatRepo;
	
	@Autowired
	private JsonUtil jsonUtil;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Value("${beckn.seller.url}")
	private String sellerUrl;

	@Value("${beckn.seller.id}")
	private String bppId;
	
	@Value("${xinput.form.base_url}")
	private String xinUrl;

	public OnSearchRequest buildOnSearch(SearchRequest request) {
		OnSearchRequest replyModel = new OnSearchRequest();

		Context context = this.responseBuilder.buildContext(request.getContext(), ContextAction.ON_SEARCH.value());
		context.setBppId(this.bppId);

		OnSearchMessage message = new OnSearchMessage();

		message.setCatalog(buildSchemeCatalog(request.getMessage()));

		context.setTimestamp(commonUtil.getDateTimeString(new Date()));
		replyModel.setContext(context);
		replyModel.setMessage(message);
		return replyModel;
	}

	private Catalog buildSchemeCatalog(SearchMessage searchMessage) {

		Catalog catalog = new Catalog();
		
		// setting BPP descriptor to catalog
		catalog.setBppDescriptor(new Descriptor());
		catalog.getBppDescriptor().setName("Protean DSEP Scholarships and Grants BPP Platform");
		
		List<Provider> bppProviders = new ArrayList<>();
		List<Fulfillment> bppFulfillments = new ArrayList<>();
		
		Map<String, DsepSchemeProvider> sellersMap = this.dao.getSchemeProviders();
		
		List<String> intentDtls = getIntentAndType(searchMessage);
		
		log.info("Intent details=>{}", intentDtls);
		Map<String, List<DsepScheme>> groupBySellers = this.dao.getSchemesGroupBySchemeProviders(intentDtls.get(0), intentDtls.get(1));

		for (Entry<String, List<DsepScheme>> entry : groupBySellers.entrySet()) {
			List<DsepScheme> schemeList = entry.getValue();

			String sellerId = entry.getKey();
			String sellerName = null;
			
			DsepSchemeProvider schemeProvider = sellersMap.get(sellerId);
			if (schemeProvider != null) {
				sellerName = schemeProvider.getDspSchemeProviderName();
			}

			if (schemeList.size() == 0) {
				return catalog;
			}

			Set<Category> categoryList = new HashSet<Category>();
			for (DsepScheme scheme : schemeList) {

				// building categories
				DsepSchemeCategory schemeCat = schemeCatRepo.findByDscCatCode(scheme.getDsSchemeFor());
				Category category = new Category();
				category.setId(InternalConstant.CATEGORY_ID_PREFIX.concat(String.valueOf(schemeCat.getDscCatId())));
				Descriptor categoryDescriptor = new Descriptor();
				categoryDescriptor.setCode(schemeCat.getDscCatCode());
				categoryDescriptor.setName(schemeCat.getDscCatName());
				category.setDescriptor(categoryDescriptor);
				categoryList.add(category);
			}

			Provider provider = new Provider();
			

			// setting seller id and name
			provider.setId(sellerId);
			provider.setDescriptor(new Descriptor());
			provider.getDescriptor().setName(sellerName);
			
			provider.setCategories(categoryList);

			// building item list
			List<Item> replyItemList = new ArrayList<>();
			for (DsepScheme entity : schemeList) {
				
				Item item = new Item();

				item.setId(entity.getDsSchemeId());

				Descriptor itemDescriptor = new Descriptor();
				itemDescriptor.setName(entity.getDsSchemeName());
				itemDescriptor.setLongDesc(entity.getDsSchemeDescription());
				item.setDescriptor(itemDescriptor);

				DsepSchemeCategory schemeCat = schemeCatRepo.findByDscCatCode(entity.getDsSchemeFor());
				List<String> catIds = new ArrayList<String>();
				catIds.add(InternalConstant.CATEGORY_ID_PREFIX.concat(String.valueOf(schemeCat.getDscCatId())));
				item.setCategoryIds(catIds);
				
				Price price = new Price();
				price.setCurrency("INR");
				price.setValue(String.valueOf(entity.getDsSchemeAmount()));
				item.setPrice(price);

				List<TagGroup> tagData = new ArrayList<>();
				
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
				tagSchemeAmt.setValue(entity.getDsSchemeAmount() > 0 ? commonUtil.getSchemeAmountString(entity.getDsSchemeAmount()):"0");
				tagSchemeAmt.setDisplay(true);
				tagDataBenefitList.add(tagSchemeAmt);
				
				tdataBenefit.setList(tagDataBenefitList);
				tagData.add(tdataBenefit);
				
				item.setTags(tagData);
				
				Fulfillment fulfillment = new Fulfillment();
				fulfillment.setId(InternalConstant.FULFILLMENT_ID_PREFIX.concat(entity.getDsSchemeId().split("_")[1]));
				fulfillment.setType(entity.getDsSchemeType());
				
				List<Stop> stops = new ArrayList<Stop>();
				
				Stop start = new Stop();
				start.setType(InternalConstant.APPLICATION_START);
				Time startTime = new Time();
				startTime.setTimestamp(commonUtil.getDateTimeString(entity.getDsStartDate()));
				start.setTime(startTime);
				
				Stop end = new Stop();
				end.setType(InternalConstant.APPLICATION_END);
				Time endTime = new Time();
				endTime.setTimestamp(commonUtil.getDateTimeString(entity.getDsEndDate()));
				end.setTime(endTime);
				
				stops.add(start);
				stops.add(end);
				fulfillment.setStops(stops);
				
				Contact contact = new Contact();
				contact.setEmail(entity.getDsSpocEmail());
				contact.setPhone(entity.getDsHelpdeskNo());
				
				fulfillment.setContact(contact);
				
				bppFulfillments.add(fulfillment);
				List<String> fulfillmentIds = new ArrayList<String>();
				fulfillmentIds.add(fulfillment.getId());
				item.setFulfillmentIds(fulfillmentIds);
				replyItemList.add(item);
			}
			
			provider.setItems(replyItemList);
			provider.setFulfillments(bppFulfillments);
			// adding the single provider
			bppProviders.add(provider);
		}

		// setting the providers to catalog
		catalog.setBppProviders(bppProviders);		
		
		log.info("### FINAL CATALOG ####");
		log.info(jsonUtil.toJson(catalog));
		log.info("######################");
		return catalog;
	}
	
	private List<String> getIntentAndType(SearchMessage message) {
		List<String> intentDtls = new ArrayList<String>();
		String intent = null;
		String intentType = null;
		
		if (message != null && message.getIntent() != null) {
			if(message.getIntent().getItem() != null && message.getIntent().getItem().getDescriptor() != null) {
				intent = message.getIntent().getItem().getDescriptor().getName();
				intentType = InternalConstant.INTENT_TYPE_SCHEME_NAME;
			}else if(message.getIntent().getFulfillment() != null && message.getIntent().getFulfillment().getCustomer() != null 
						&& message.getIntent().getFulfillment().getCustomer().getPerson().getGender() != null) {
				intent =  message.getIntent().getFulfillment().getCustomer().getPerson().getGender();
				intentType = InternalConstant.INTENT_TYPE_GENDER;
				
				if(message.getIntent().getProvider() != null && message.getIntent().getProvider().getCategories() != null 
						&& message.getIntent().getProvider().getCategories().size() > 0) {
					Set<Category> categories = message.getIntent().getProvider().getCategories();
					List<String> cat=new ArrayList<String>();
					for (Category category : categories) {
						cat.add(category.getDescriptor().getCode());
					}
					intent =  intent.concat("|").concat(String.join(",", cat));
					intentType = InternalConstant.INTENT_TYPE_GENDER_COURSE_CAT;
				}
			}else if(message.getIntent().getFulfillment() != null && message.getIntent().getFulfillment().getCustomer() != null 
					&& message.getIntent().getFulfillment().getCustomer().getPerson().getTags() != null
					&& message.getIntent().getFulfillment().getCustomer().getPerson().getTags().size() > 0) {
				
				List<TagGroup> tagData = message.getIntent().getFulfillment().getCustomer().getPerson().getTags();
				
				for (TagGroup tagGrop : tagData) {
					if(tagGrop.getDescriptor().getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_CODE)) {
						List<Tag> tagDataList = tagGrop.getList();
						if(tagDataList.get(0).getDescriptor().getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_CODE)) {
							intent =  tagDataList.get(0).getValue();
							intentType = InternalConstant.INTENT_TYPE_MAX_FAMILIY_INCOME;
							break;
						}
					}else if(tagGrop.getDescriptor().getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_CODE)) {
						List<Tag> tagDataList = tagGrop.getList();
						intent =  tagDataList.get(0).getValue();
						intentType = InternalConstant.INTENT_TYPE_CASTE;
						break;
					}
				}
			}
			
			intentDtls.add(intent);
			intentDtls.add(intentType);
		}
		
		
		return intentDtls;
	}
}