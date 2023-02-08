package com.protean.dsep.bpp.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.protean.dsep.bpp.builder.CommonBuilder;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.dao.SchemeRepo;
import com.protean.dsep.bpp.entity.DsepScheme;
import com.protean.dsep.bpp.model.SchemeModel;
import com.protean.dsep.bpp.util.JsonUtil;
import com.protean.dsep.bpp.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class SchemeService {

	@Autowired
	private SchemeRepo dao;

	@Autowired
	private CommonBuilder builder;

	@Autowired
	private SecurityUtil securityUtil;

	@Autowired
	private JsonUtil jsonUtil;
	
	@Value("${dsep.scholarship_grant.providerId}")
	private String providerID;
	
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(InternalConstant.SCHEME_DATE_FORMAT);
	
	public String createScheme(SchemeModel model) throws ParseException {

		model.setSchemeProviderID(providerID);
		model.setCreatedBy(securityUtil.getCurrentUserName());
		model.setCreatedIP(securityUtil.getRemoteIPAddr());
		model.setPublished(false);
		model.setDeleted(false);
		
		DsepScheme entity = this.builder.buildSchemeEntity(model);
		DsepScheme schemeData = null;
		
		try {
			schemeData = this.dao.save(entity);
			log.info("Scheme is created [id={}, schemeId={}]", schemeData.getDsId(), schemeData.getDsSchemeId());
		} catch (Exception e) {
			throw e;
		}

		return schemeData != null ? schemeData.getDsSchemeId() : null;
	}

	public List<SchemeModel> getSchemeList(String userId) {
		List<DsepScheme> entityList = null;
		try {
			if (userId == null || this.securityUtil.getCurrentUserName() == null || !this.securityUtil.getCurrentUserName().equals(userId)) {
				throw new RuntimeException("Invalid Scheme Provider ID");
			}
			
			entityList = this.dao.findByDsSchemeProviderIdAndDsIsDeleted(userId,false);
			log.info("Total Schemes - {}", entityList.size());
			
		} catch (Exception e) {
			throw e;
		}

		return entityList
				.stream()
				.map(this.builder::buildSchemeModel)
				.collect(Collectors.toList());

	}
	
	public List<SchemeModel> getSchemeList() {
		List<DsepScheme> entityList = null;
		try {
			entityList = this.dao.findByDsSchemeProviderIdAndDsIsDeletedOrderByUpdatedAtDesc(providerID, Boolean.FALSE);
			
			log.info("Total Schemes - {}", entityList.size());
		} catch (Exception e) {
			throw e;
		}

		return entityList
				.stream()
				.map(this.builder::buildSchemeModel)
				.collect(Collectors.toList());

	}

	public SchemeModel getDetailsByID(String schemeID) {
		SchemeModel schemeDtls = null;
		
		try {
			DsepScheme scheme = this.dao.findById(UUID.fromString(schemeID)).get();
			if(scheme != null) {
				schemeDtls = this.builder.buildSchemeModel(scheme);
			}else {
				throw new EntityNotFoundException(schemeID+" not found");
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return schemeDtls;
	}
	
	public SchemeModel getDetailsBySchemeID(String schemeID) {
		SchemeModel schemeDtls = null;
		
		try {
			DsepScheme scheme = this.dao.findByDsSchemeId(schemeID);
			if(scheme != null) {
				schemeDtls = this.builder.buildSchemeModel(scheme);
			}else {
				throw new EntityNotFoundException(schemeID+" not found");
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return schemeDtls;
	}

	public boolean updateScheme(String schemeID, SchemeModel model) throws Exception {
		boolean result = false;
		model.setUpdatedBy(securityUtil.getCurrentUserName());
		model.setUpdatedIP(securityUtil.getRemoteIPAddr());
		
		try {
			DsepScheme scheme = this.dao.findByDsSchemeId(schemeID);
			if(scheme != null) {
				scheme.setDsSchemeType(model.getSchemeType());
				scheme.setDsSchemeName(model.getSchemeName());
				scheme.setDsSchemeDescription(model.getSchemeDescription());
				scheme.setDsSchemeFor(model.getSchemeFor());
				scheme.setDsFinancialYear(model.getFinancialYear());
				scheme.setDsSchemeAmount(model.getSchemeAmount());
				scheme.setDsEligibility(jsonUtil.toJson(model.getEligibility()));
				scheme.setDsStartDate(simpleDateFormat.parse(model.getStartDate()));
				scheme.setDsEndDate(simpleDateFormat.parse(model.getEndDate()));
				scheme.setDsAddtnlInfoReq(model.isAddtnlInfoReq());
				scheme.setDsSpocEmail(model.getSpocEmail());
				scheme.setDsSpocName(model.getSpocName());
				scheme.setDsHelpdeskNo(model.getHelpdeskNo());				
				scheme.setUpdatedBy(model.getUpdatedBy());
				scheme.setUpdatedIP(model.getUpdatedIP());
								
				this.dao.save(scheme);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException(schemeID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

	public boolean publishUnpublishScheme(String schemeID, boolean publishFlag) {
		boolean result = false;
		try {
			DsepScheme scheme = this.dao.findByDsSchemeId(schemeID);
			
			if(scheme != null) {
				scheme.setDsIsPublished(publishFlag);
				scheme.setUpdatedBy(securityUtil.getCurrentUserName());
				scheme.setUpdatedIP(securityUtil.getRemoteIPAddr());

				this.dao.save(scheme);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException(schemeID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}
	
	public boolean deleteScheme(String schemeID) {
		boolean result = false;
		try {
			DsepScheme scheme = this.dao.findByDsSchemeId(schemeID);
			
			if(scheme != null) {
				scheme.setDsIsDeleted(Boolean.TRUE);
				scheme.setUpdatedBy(securityUtil.getCurrentUserName());
				scheme.setUpdatedIP(securityUtil.getRemoteIPAddr());

				this.dao.save(scheme);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException(schemeID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

}
