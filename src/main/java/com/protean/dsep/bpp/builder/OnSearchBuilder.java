package com.protean.dsep.bpp.builder;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import com.protean.beckn.api.model.common.Customer;
import com.protean.beckn.api.model.common.Descriptor;
import com.protean.beckn.api.model.common.End;
import com.protean.beckn.api.model.common.Fulfillment;
import com.protean.beckn.api.model.common.Item;
import com.protean.beckn.api.model.common.Person;
import com.protean.beckn.api.model.common.Price;
import com.protean.beckn.api.model.common.Provider;
import com.protean.beckn.api.model.common.Start;
import com.protean.beckn.api.model.common.TagData;
import com.protean.beckn.api.model.common.TagDataList;
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
import com.protean.dsep.bpp.model.AcademicDtlsModel;
import com.protean.dsep.bpp.model.SchemeEligibilityModel;

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

		context.setTimestamp(commonUtil.getDateTimeString());
		replyModel.setContext(context);
		replyModel.setMessage(message);
		return replyModel;
	}

	private Catalog buildSchemeCatalog(SearchMessage searchMessage) {

		// starting
		Catalog catalog = new Catalog();
		List<Provider> bppProviders = new ArrayList<>();
		List<Fulfillment> bppFulfillments = new ArrayList<>();
		
		String bppDescriptorVal=null;

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
				bppDescriptorVal = sellerName;
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
			Descriptor providerDescriptor = new Descriptor();
			providerDescriptor.setName(sellerName);
			provider.setDescriptor(providerDescriptor);
			
			provider.setCategories(categoryList);

			// building item list
			List<Item> replyItemList = new ArrayList<>();
			int itemIndex = 0 ;
			for (DsepScheme entity : schemeList) {
				itemIndex++;
				
				Item item = new Item();

				item.setId(entity.getDsSchemeId());

				Descriptor itemDescriptor = new Descriptor();
				itemDescriptor.setName(entity.getDsSchemeName());
				itemDescriptor.setLongDesc(entity.getDsSchemeDescription());
				item.setDescriptor(itemDescriptor);

				DsepSchemeCategory schemeCat = schemeCatRepo.findByDscCatCode(entity.getDsSchemeFor());
				item.setCategoryId(InternalConstant.CATEGORY_ID_PREFIX.concat(String.valueOf(schemeCat.getDscCatId())));
				Price price = new Price();
				price.setCurrency("INR");
				price.setValue(String.valueOf(entity.getDsSchemeAmount()));
				item.setPrice(price);

				SchemeEligibilityModel eligibilityData = jsonUtil.toModel(entity.getDsEligibility(), SchemeEligibilityModel.class)  ;
				
				Customer customer = null;
				
				if(eligibilityData != null) {
					customer = new Customer();
					
					Person person = new Person();
					
					List<TagData> tagData = new ArrayList<TagData>();
					
					if(eligibilityData.getGender() != null && !eligibilityData.getGender().isBlank()) {
						person.setGender(eligibilityData.getGender().trim());
					}
					
					List<AcademicDtlsModel> acadDtls =  eligibilityData.getAcadDtls();
					if(acadDtls != null && acadDtls.size()>0) {
						for (AcademicDtlsModel acad : acadDtls) {
							TagData tdata = new TagData();
							tdata.setCode(InternalConstant.CATALOG_SCHEME_ELG_ACAD_QUAL_CODE);
							tdata.setName(InternalConstant.CATALOG_SCHEME_ELG_ACAD_QUAL_NAME);
							
							List<TagDataList> tagDataList = new ArrayList<TagDataList>();
							
							TagDataList tdataList1 = new TagDataList();
							tdataList1.setCode(acad.getCourseLevelID());
							tdataList1.setName(acad.getCourseLevelName());
							tdataList1.setValue(acad.getCourseName());
							tagDataList.add(tdataList1);
							
							TagDataList tdataList2 = new TagDataList();
							tdataList2.setCode(InternalConstant.CATALOG_SCHEME_ELG_PERCTG_GRAD_CODE);
							tdataList2.setName(InternalConstant.CATALOG_SCHEME_ELG_PERCTG_GRAD_NAME);
							tdataList2.setValue(acad.getScoreValue());
							tagDataList.add(tdataList2);
							
							TagDataList tdataList3 = new TagDataList();
							tdataList3.setCode(InternalConstant.CATALOG_SCHEME_ELG_PASS_YR_CODE);
							tdataList3.setName(InternalConstant.CATALOG_SCHEME_ELG_PASS_YR_NAME);
							tdataList3.setValue(acad.getPassingYear());
							tagDataList.add(tdataList3);
							
							tdata.setList(tagDataList);
							
							tagData.add(tdata);
						}
					}
					
					
					if(eligibilityData.getCaste() != null && eligibilityData.getCaste().size() > 0) {
						TagData tdataCast = new TagData();
						tdataCast.setCode(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_CODE);
						tdataCast.setName(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_NAME);
						
						List<TagDataList> tagDataCastList = new ArrayList<TagDataList>();
												
						for (String castData : eligibilityData.getCaste()) {
							TagDataList tdataCastList = new TagDataList();
							tdataCastList.setCode(castData.trim());
							tdataCastList.setValue(castData.trim());
							tagDataCastList.add(tdataCastList);
						}
						tdataCast.setList(tagDataCastList);
						tagData.add(tdataCast);
					}
					
					if(eligibilityData.getFamilyIncome() != null && !eligibilityData.getFamilyIncome().isBlank()) {
						TagData tdataFin = new TagData();
						tdataFin.setCode(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_CODE);
						tdataFin.setName(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_NAME);
						
						List<TagDataList> tagDataFinList = new ArrayList<TagDataList>();
						
						TagDataList tdataFinList = new TagDataList();
						tdataFinList.setCode(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_CODE);
						tdataFinList.setName(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_NAME);
						tdataFinList.setValue(eligibilityData.getFamilyIncome().trim());
						tagDataFinList.add(tdataFinList);
						tdataFin.setList(tagDataFinList);
						tagData.add(tdataFin);
					}

					person.setTags(tagData);
					customer.setPerson(person);
				}	
				
				Fulfillment fulfillment = new Fulfillment();
				fulfillment.setId(InternalConstant.FULFILLMENT_ID_PREFIX.concat(String.format("%02d", itemIndex)));
				fulfillment.setType(entity.getDsSchemeType());
				
				if(customer != null) {
					fulfillment.setCustomer(customer);
				}
				
				Start start = new Start();
				Time startTime = new Time();
				startTime.setTimestamp(entity.getDsStartDate().toString());
				start.setTime(startTime);
				
				End end = new End();
				Time endTime = new Time();
				endTime.setTimestamp(entity.getDsEndDate().toString());
				end.setTime(endTime);
				
				fulfillment.setStart(start);
				fulfillment.setEnd(end);
				
				Contact contact = new Contact();
				contact.setName(entity.getDsSpocName());
				contact.setEmail(entity.getDsSpocEmail());
				contact.setPhone(entity.getDsHelpdeskNo());
				
				fulfillment.setContact(contact);
				
				bppFulfillments.add(fulfillment);
				item.setFulfillmentId(fulfillment.getId());
				//item.setMatched(Boolean.TRUE);				
				replyItemList.add(item);
			}
			
			provider.setItems(replyItemList);
			provider.setFulfillments(bppFulfillments);
			// adding the single provider
			bppProviders.add(provider);
		}

		// setting the providers to catalog
		catalog.setBppProviders(bppProviders);

		// setting BPP descriptor to catalog
		Descriptor bppDescriptor = new Descriptor();
		bppDescriptor.setName(bppDescriptorVal);
		catalog.setBppDescriptor(bppDescriptor);

		// setting BPP fulfillment to catalog
		
		
		//catalog.setBppFulfillments(bppFulfillments);
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
				
				List<TagData> tagData = message.getIntent().getFulfillment().getCustomer().getPerson().getTags();
				
				for (TagData tag : tagData) {
					if(tag.getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_FIN_CAT_CODE)) {
						List<TagDataList> tagDataList = tag.getList();
						if(tagDataList.get(0).getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_FMLY_INCOM_CODE)) {
							intent =  tagDataList.get(0).getValue();
							intentType = InternalConstant.INTENT_TYPE_MAX_FAMILIY_INCOME;
							break;
						}
					}else if(tag.getCode().equalsIgnoreCase(InternalConstant.CATALOG_SCHEME_ELG_CASTE_CAT_CODE)) {
						List<TagDataList> tagDataList = tag.getList();
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