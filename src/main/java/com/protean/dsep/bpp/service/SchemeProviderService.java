package com.protean.dsep.bpp.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.protean.dsep.bpp.builder.CommonBuilder;
import com.protean.dsep.bpp.dao.SchemeProviderRepo;
import com.protean.dsep.bpp.entity.DsepSchemeProvider;
import com.protean.dsep.bpp.model.SchemeProviderModel;
import com.protean.dsep.bpp.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class SchemeProviderService {

	@Autowired
	private SchemeProviderRepo dao;

	@Autowired
	private CommonBuilder builder;

	@Autowired
	private SecurityUtil securityUtil;
	
	public String addSchemeProvider(SchemeProviderModel model) {

		model.setCreatedBy(securityUtil.getCurrentUserName());
		model.setCreatedIP(securityUtil.getRemoteIPAddr());
		model.setActive(true);
		model.setDeleted(false);
		
		DsepSchemeProvider entity = this.builder.buildSchemeProviderEntity(model);
		DsepSchemeProvider spData = null;
		
		try {
			spData = this.dao.save(entity);
			log.info("Scheme provider is added [id={}, schemeProviderId={}]", spData.getDspId(), spData.getDspSchemeProviderId());
		} catch (Exception e) {
			throw e;
		}

		return spData != null ? spData.getDspSchemeProviderId() : null;
	}

	public List<SchemeProviderModel> getSchemeProviderList() {
		List<DsepSchemeProvider> entityList = null;
		try {
			
			entityList = this.dao.findAll();
			log.info("Total Scheme Providers - {}", entityList.size());
			
		} catch (Exception e) {
			throw e;
		}

		return entityList
				.stream()
				.map(this.builder::buildSchemeProviderModel)
				.collect(Collectors.toList());

	}
	
	public SchemeProviderModel getDetailsBySchemeProviderID(String spID) {
		SchemeProviderModel spDtls = null;
		
		try {
			DsepSchemeProvider schemeProvider = this.dao.findByDspSchemeProviderId(spID);
			if(schemeProvider != null) {
				spDtls = this.builder.buildSchemeProviderModel(schemeProvider);
			}else {
				throw new EntityNotFoundException("ProviderID-"+spID+" not found");
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return spDtls;
	}

	public SchemeProviderModel getDetailsByID(String spID) {
		SchemeProviderModel spDtls = null;
		
		try {
			DsepSchemeProvider schemeProvider = this.dao.findById(spID).get();
			if(schemeProvider != null) {
				spDtls = this.builder.buildSchemeProviderModel(schemeProvider);
			}else {
				throw new EntityNotFoundException("ProviderID-"+spID+" not found");
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return spDtls;
	}
	
	public boolean updateSchemeProvider(String spID, SchemeProviderModel model) {
		boolean result = false;
		model.setUpdatedBy(securityUtil.getCurrentUserName());
		model.setUpdatedIP(securityUtil.getRemoteIPAddr());
		
		try {
			DsepSchemeProvider spDtls = this.dao.findByDspSchemeProviderId(spID);
			if(spDtls != null) {
				spDtls.setDspSchemeProviderName(model.getSchemeProviderName());
				spDtls.setDspSchemeProviderDescription(model.getSchemeProviderDescription());
				spDtls.setDspSchemeProviderWebsite(model.getSchemeProviderWebsite());
				spDtls.setUpdatedBy(model.getUpdatedBy());
				spDtls.setUpdatedIP(model.getUpdatedIP());
								
				this.dao.save(spDtls);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException("ProviderID-"+spID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

	public boolean activateDeactivateSchemeProvider(String spID, boolean activeFlag) {
		boolean result = false;
		try {
			DsepSchemeProvider spDtls = this.dao.findByDspSchemeProviderId(spID);
			
			if(spDtls != null) {
				spDtls.setDspIsActive(activeFlag);
				spDtls.setUpdatedBy(securityUtil.getCurrentUserName());
				spDtls.setUpdatedIP(securityUtil.getRemoteIPAddr());

				this.dao.save(spDtls);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException("ProviderID-"+spID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}
	
	public boolean deleteSchemeProvider(String spID) {
		boolean result = false;
		try {
			DsepSchemeProvider spDtls = this.dao.findByDspSchemeProviderId(spID);
			
			if(spDtls != null) {
				spDtls.setDspIsDeleted(Boolean.TRUE);
				spDtls.setUpdatedBy(securityUtil.getCurrentUserName());
				spDtls.setUpdatedIP(securityUtil.getRemoteIPAddr());

				this.dao.save(spDtls);
				
				result = true;
			}else {
				result = false;
				throw new EntityNotFoundException("ProviderID-"+spID+" not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

}
