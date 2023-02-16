package com.protean.dsep.bpp.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;

import net.bytebuddy.utility.RandomString;

import com.protean.beckn.api.model.common.Customer;
import com.protean.beckn.api.model.common.Order;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.entity.ApplicationDtlsView;
import com.protean.dsep.bpp.entity.DsepApplicationDtl;
import com.protean.dsep.bpp.entity.DsepScheme;
import com.protean.dsep.bpp.entity.DsepSchemeProvider;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.ApplicationDtlsViewModel;
import com.protean.dsep.bpp.model.SchemeEligibilityModel;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.model.SchemeProviderModel;

@Component
public class CommonBuilder {

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	private CommonUtil commonUtil;
	
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(InternalConstant.SCHEME_DATE_FORMAT);
	
	SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(InternalConstant.DATE_TIME_FORMAT);
	
	public DsepScheme buildSchemeEntity(SchemeModel model) throws ParseException {
		String schemeId = model.getSchemeID();
		if(null == schemeId || schemeId.isBlank() || schemeId.isEmpty()) {
		    schemeId = commonUtil.generateID(InternalConstant.SCHEME_ID_PREFIX, "%s%08d");
		}
		
		DsepScheme entity = new DsepScheme();
		entity.setDsSchemeId(schemeId);
		entity.setDsSchemeType(model.getSchemeType());
		entity.setDsSchemeName(model.getSchemeName());
		entity.setDsSchemeDescription(model.getSchemeDescription());
	
		entity.setDsSchemeFor(model.getSchemeFor());
		entity.setDsFinancialYear(model.getFinancialYear());
		entity.setDsSchemeAmount(model.getSchemeAmount());

		entity.setDsSchemeProviderId(model.getSchemeProviderID());
		entity.setDsEligibility(jsonUtil.toJson(model.getEligibility()));
		entity.setDsStartDate(simpleDateFormat.parse(model.getStartDate()));
		entity.setDsEndDate(simpleDateFormat.parse(model.getEndDate()));
		entity.setDsAddtnlInfoReq(model.isAddtnlInfoReq());
		entity.setDsSpocEmail(model.getSpocEmail());
		entity.setDsSpocName(model.getSpocName());
		entity.setDsHelpdeskNo(model.getHelpdeskNo());
		entity.setDsIsPublished(model.isPublished());
		entity.setDsIsDeleted(model.isDeleted());
		entity.setCreatedBy(model.getCreatedBy());
		entity.setCreatedIP(model.getCreatedIP());
		return entity;
	}
	
	public SchemeModel buildSchemeModel(DsepScheme entity) {
		SchemeModel model = new SchemeModel();
		model.setId(entity.getDsId());
		model.setSchemeID(entity.getDsSchemeId());
		model.setSchemeType(entity.getDsSchemeType());
		model.setSchemeName(entity.getDsSchemeName());
		model.setSchemeDescription(entity.getDsSchemeDescription());

		model.setSchemeFor(entity.getDsSchemeFor());
		model.setFinancialYear(entity.getDsFinancialYear());
		model.setSchemeAmount(entity.getDsSchemeAmount());

		model.setSchemeProviderID(entity.getDsSchemeProviderId());
		
		model.setEligibility(jsonUtil.toModel(entity.getDsEligibility(), SchemeEligibilityModel.class));
		model.setStartDate(simpleDateFormat.format(entity.getDsStartDate()));
		model.setEndDate(simpleDateFormat.format(entity.getDsEndDate()));
		model.setAddtnlInfoReq(entity.isDsAddtnlInfoReq());
		model.setSpocEmail(entity.getDsSpocEmail());
		model.setSpocName(entity.getDsSpocName());
		model.setHelpdeskNo(entity.getDsHelpdeskNo());
		model.setPublished(entity.isDsIsPublished());
		model.setDeleted(entity.isDsIsDeleted());
		model.setCreatedBy(entity.getCreatedBy());
		model.setCreatedIP(entity.getCreatedIP());
		model.setCreatedAt(simpleDateTimeFormat.format(entity.getCreatedAt()));
		model.setUpdatedBy(entity.getUpdatedBy());
		model.setUpdatedAt(simpleDateTimeFormat.format(entity.getUpdatedAt()));
		model.setUpdatedIP(entity.getUpdatedIP());
		return model;
	}
	
	public DsepSchemeProvider buildSchemeProviderEntity(SchemeProviderModel model) {
		String spID = model.getSchemeProviderId();
		if(null == spID || spID.isBlank() || spID.isEmpty()) {
		    RandomString rndStr = new RandomString(3);
		    String pfx = rndStr.nextString().toUpperCase();
		    spID = commonUtil.generateID(pfx, "%s%08d");
		}
		
		DsepSchemeProvider entity = new DsepSchemeProvider();
		entity.setDspSchemeProviderId(spID);
		entity.setDspSchemeProviderName(model.getSchemeProviderName());
		entity.setDspSchemeProviderDescription(model.getSchemeProviderDescription());
		entity.setDspSchemeProviderWebsite(model.getSchemeProviderWebsite());
		entity.setDspIsActive(model.isActive());
		entity.setCreatedBy(model.getCreatedBy());
		entity.setCreatedIP(model.getCreatedIP());
		entity.setUpdatedBy(model.getUpdatedBy());
		entity.setUpdatedIP(model.getUpdatedIP());
		
		return entity;
	}
	
	public SchemeProviderModel buildSchemeProviderModel(DsepSchemeProvider entity) {
		SchemeProviderModel model = new SchemeProviderModel();
		model.setId(entity.getDspId());
		model.setSchemeProviderId(entity.getDspSchemeProviderId());
		model.setSchemeProviderName(entity.getDspSchemeProviderName());
		model.setSchemeProviderDescription(entity.getDspSchemeProviderDescription());
		model.setSchemeProviderWebsite(entity.getDspSchemeProviderWebsite());
		model.setActive(entity.isDspIsActive());
		model.setCreatedBy(entity.getCreatedBy());
		model.setCreatedIP(entity.getCreatedIP());
		model.setCreatedAt(entity.getCreatedAt());
		model.setUpdatedBy(entity.getUpdatedBy());
		model.setUpdatedAt(entity.getUpdatedAt());
		model.setUpdatedIP(entity.getUpdatedIP());
		return model;
	}
	
	public DsepApplicationDtl buildApplicationDtlEntity(ApplicationDtlModel model) {
		DsepApplicationDtl entity = new DsepApplicationDtl();
		String appID = model.getAppId();
		String addInfoID = model.getAddtnlInfoId();
		String nonceVal = model.getXinputNonceVal();
		if(null == appID || appID.isBlank() || appID.isEmpty()) {
			appID = commonUtil.generateID(InternalConstant.APP_ID_PREFIX, "%s%08d");
			addInfoID = UUID.randomUUID().toString().replace("-", "");
			nonceVal = commonUtil.generateNonceVal(32);
		}
		
		entity.setDadAppId(appID);
		entity.setDadDsepTxnId(model.getDsepTxnId());
		entity.setDadAddtnlInfoId(addInfoID);
		entity.setDadXinputNonceVal(nonceVal);
		entity.setDadAddtnlDtls(model.getAddtnlDtls());
		entity.setDadAddtnlInfoSubmsnId(model.getAddtnlInfoSubmsnId());
		entity.setDadApplcntId(model.getApplcntId());
		entity.setDadApplcntDtls(model.getApplcntDtls() != null ? jsonUtil.toJson(model.getApplcntDtls()):null);
		entity.setDadAppStatus(model.getAppStatus());
		entity.setDadDeleted(model.isDeleted());
		entity.setDadSchemeId(model.getSchemeId());
		entity.setDadSchemeProviderId(model.getSchemeProviderId());
		entity.setDadRemarks(model.getRemarks());
		entity.setCreatedBy(model.getCreatedBy());
		entity.setCreatedIP(model.getCreatedIP());
		entity.setUpdatedBy(model.getUpdatedBy());
		entity.setUpdatedIP(model.getUpdatedIP());
		return entity;
	}
	
	public ApplicationDtlModel buildApplicationDtlModel(DsepApplicationDtl entity) {
		ApplicationDtlModel model = new ApplicationDtlModel();
		
		model.setId(entity.getDadId());
		model.setAppId(entity.getDadAppId());
		model.setDsepTxnId(entity.getDadDsepTxnId());
		model.setAddtnlInfoId(entity.getDadAddtnlInfoId());
		model.setXinputNonceVal(entity.getDadXinputNonceVal());
		model.setAddtnlDtls(entity.getDadAddtnlDtls());
		model.setAddtnlInfoSubmsnId(entity.getDadAddtnlInfoSubmsnId());
		model.setApplcntId(entity.getDadApplcntId());
		model.setApplcntDtls(entity.getDadApplcntDtls() != null ? jsonUtil.toModel(entity.getDadApplcntDtls(), Customer.class):null);
		model.setAppStatus(entity.getDadAppStatus());
		model.setDeleted(entity.getDadDeleted());
		model.setSchemeId(entity.getDadSchemeId());
		model.setSchemeProviderId(entity.getDadSchemeProviderId());
		model.setRemarks(entity.getDadRemarks());
		model.setCreatedBy(entity.getCreatedBy());
		model.setCreatedIP(entity.getCreatedIP());
		model.setCreatedAt(simpleDateTimeFormat.format(entity.getCreatedAt()));
		model.setUpdatedBy(entity.getUpdatedBy());
		model.setUpdatedAt(simpleDateTimeFormat.format(entity.getUpdatedAt()));
		model.setUpdatedIP(entity.getUpdatedIP());
		return model;
	}

	public ApplicationDtlsViewModel buildApplicationDtlViewModel(ApplicationDtlsView entity) {
		ApplicationDtlsViewModel model = new ApplicationDtlsViewModel();
		
		model.setId(entity.getDadId());
		model.setAppId(entity.getDadAppId());
		model.setAppStatus(entity.getDadAppStatus());
		model.setApplcntDtls(entity.getDadApplcntDtls() != null ? jsonUtil.toModel(entity.getDadApplcntDtls(), Customer.class) : null);
		model.setApplcntId(entity.getDadApplcntId());
		model.setAddtnlDtls(entity.getDadAddtnlDtls());
		model.setAddtnlInfoId(entity.getDadAddtnlInfoId());
		model.setAddtnlInfoSubmsnId(entity.getDadAddtnlInfoSubmsnId());
		model.setDeleted(entity.getDadDeleted());
		model.setDsepTxnId(entity.getDadDsepTxnId());
		model.setRemarks(entity.getDadRemarks());
		model.setSchemeId(entity.getDadSchemeId());
		model.setSchemeProviderId(entity.getDadSchemeProviderId());
		model.setXinputNonceVal(entity.getDadXinputNonceVal());
		model.setAddtnlInfoReq(entity.getDsAddtnlInfoReq());
		model.setFinancialYear(entity.getDsFinancialYear());
		model.setPublished(entity.getDsIsPublished());
		model.setSchemeAmount(entity.getDsSchemeAmount());
		model.setSchemeDescription(entity.getDspSchemeProviderDescription());
		model.setSchemeFor(entity.getDsSchemeFor());
		model.setSchemeName(entity.getDsSchemeName());
		model.setSchemeType(entity.getDsSchemeType());
		model.setSpocEmail(entity.getDsSpocEmail());
		model.setSpocName(entity.getDsSpocName());
		model.setHelpdeskNo(entity.getDsHelpdeskNo());
		model.setSchemeStartDate(String.valueOf(entity.getDsStartDate()));
		model.setSchemeEndDate(String.valueOf(entity.getDsEndDate()));
		model.setSchemeProviderActive(entity.getDspIsActive());
		model.setSchemeProviderDescription(entity.getDspSchemeProviderDescription());
		model.setSchemeProviderName(entity.getDspSchemeProviderName());
		model.setSchemeProviderWebsite(entity.getDspSchemeProviderWebsite());
		model.setAppCreatedAt(String.valueOf(entity.getCreatedAt()));
		model.setAppCreatedBy(entity.getCreatedBy());
		model.setAppCreatedIp(entity.getCreatedIp());
		model.setAppUpdatedAt(String.valueOf(entity.getUpdatedAt()));
		model.setAppUpdatedBy(entity.getUpdatedBy());
		model.setAppUpdatedIp(entity.getUpdatedIp());
		return model;
	}
}
