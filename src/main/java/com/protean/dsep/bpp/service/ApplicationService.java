package com.protean.dsep.bpp.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;
import com.protean.beckn.api.model.common.Customer;
import com.protean.dsep.bpp.builder.CommonBuilder;
import com.protean.dsep.bpp.constant.ApplicationStatus;
import com.protean.dsep.bpp.dao.ApplicationDtlRepo;
import com.protean.dsep.bpp.dao.ApplicationDtlsViewRepo;
import com.protean.dsep.bpp.entity.ApplicationDtlsView;
import com.protean.dsep.bpp.entity.DsepApplicationDtl;
import com.protean.dsep.bpp.model.ApplicationDtlModel;
import com.protean.dsep.bpp.model.ApplicationDtlsViewModel;
import com.protean.dsep.bpp.util.CommonUtil;
import com.protean.dsep.bpp.util.JsonUtil;
import com.protean.dsep.bpp.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationService {

	@Autowired
	ApplicationDtlRepo appDtlRepo;

	@Autowired
	ApplicationDtlsViewRepo appDtlViewRepo;
	
	@Autowired
	SecurityUtil securityUtil;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	JsonUtil jsonUtil;

	@Autowired
	CommonBuilder commonBuilder;

	public ApplicationDtlModel initApplication(ApplicationDtlModel model) throws Exception {
		ApplicationDtlModel appModel = null;
		model.setUpdatedBy(model.getApplcntId());
		model.setDeleted(false);
		DsepApplicationDtl appEntity = null;

		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadDsepTxnId(model.getDsepTxnId());
			if(entity == null) {
				appEntity = commonBuilder.buildApplicationDtlEntity(model);
				log.info("Saving init application details - {}", appEntity);
				
			} else {
				appEntity = entity;
				appEntity.setDadDsepTxnId(model.getDsepTxnId());
				appEntity.setDadAppStatus(model.getAppStatus());
				appEntity.setDadSchemeId(model.getSchemeId());
				appEntity.setDadSchemeProviderId(model.getSchemeProviderId());
				appEntity.setUpdatedBy(model.getUpdatedBy());
				log.info("Updating init application details - {}", entity);
			}
			
			DsepApplicationDtl appEntityNew = appDtlRepo.save(appEntity);
			appModel = commonBuilder.buildApplicationDtlModel(appEntityNew);
			
			log.info("INIT application created successfully - ", appModel);
		} catch (Exception e) {
			log.error("Exception occurred while saving application details-", e);
			appModel = null;
			throw e;
		}
		return appModel;
	}

	public ApplicationDtlModel confirmApplication(ApplicationDtlModel model) throws Exception {
		ApplicationDtlModel appModel = null;
		DsepApplicationDtl appEntity = null;

		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadAppId(model.getAppId());
			if (entity != null) {
				entity.setDadSchemeId(model.getSchemeId());
				entity.setDadSchemeProviderId(model.getSchemeProviderId());
				entity.setDadApplcntDtls(model.getApplcntDtls() != null ? jsonUtil.toJson(model.getApplcntDtls()) : null);
				entity.setDadAddtnlDtls(model.getAddtnlDtls());
				entity.setDadAddtnlInfoSubmsnId(model.getAddtnlInfoSubmsnId());
				
				entity.setDadAppStatus(model.getAppStatus());
				entity.setDadRemarks(model.getRemarks());
				entity.setUpdatedBy(model.getApplcntId());
				appEntity = appDtlRepo.save(entity);
				appModel = commonBuilder.buildApplicationDtlModel(appEntity);
			} else {
				throw new EntityNotFoundException("Application not found for AppID-" + model.getAppId());
			}
		} catch (Exception e) {
			throw e;
		}
		
		return appModel;
	}
	
	public String getAppAddtnlInfoNonceValue(String txnID, String addInfoID) {
		String nonceVal = null;
		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadDsepTxnIdAndDadAddtnlInfoId(txnID, addInfoID);
			if (entity != null) {
				nonceVal = entity.getDadXinputNonceVal();
			} else {
				throw new EntityNotFoundException(
						"Application not found for txID-" + txnID + ", ddInfoID-" + addInfoID);
			}
		} catch (Exception e) {
			throw e;
		}

		return nonceVal;
	}

	public ApplicationDtlModel getApplicationDtlByAppIdAndAddInfoID(String appID, String addInfoID) {
		ApplicationDtlModel model = null;
		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadAppIdAndDadAddtnlInfoId(appID, addInfoID);
			if (entity != null) {
				model = commonBuilder.buildApplicationDtlModel(entity);
			} else {
				throw new EntityNotFoundException(
						"Application not found for AppID-" + appID + ", ddInfoID-" + addInfoID);
			}
		} catch (Exception e) {
			throw e;
		}

		return model;
	}

	public String saveAppAddtnlInfo(ApplicationDtlModel model) {
		String submsnID = null;

		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadDsepTxnIdAndDadAddtnlInfoId(model.getDsepTxnId(),
					model.getAddtnlInfoId());
			if (entity != null) {
				submsnID = UUID.randomUUID().toString();
				entity.setDadAddtnlInfoSubmsnId(submsnID);
				entity.setDadAddtnlDtls(model.getAddtnlDtls());
				//Customer applicant = jsonUtil.toModel(entity.getDadApplcntDtls(), Customer.class);
				//entity.setUpdatedBy(applicant.getPerson().getId());
				appDtlRepo.save(entity);
			} else {
				throw new EntityNotFoundException("Application not found for txnID-" + model.getDsepTxnId() + ", ddInfoID-"
						+ model.getAddtnlInfoId());
			}
		} catch (Exception e) {
			throw e;
		}

		return submsnID;
	}

	public ApplicationDtlModel getApplicationDtlByXinputNonceVal(String nonceVal) {
		ApplicationDtlModel model = null;

		try {
			DsepApplicationDtl entity = appDtlRepo.findByDadXinputNonceVal(nonceVal);

			if (entity != null) {
				model = commonBuilder.buildApplicationDtlModel(entity);
			} else {
				throw new EntityNotFoundException("Application not found for nonce value-" + nonceVal);
			}
		} catch (Exception e) {
			throw e;
		}

		return model;
	}

	public boolean updateStatus(ApplicationDtlModel model, int value) {
		boolean result = false;
		model.setUpdatedBy(securityUtil.getCurrentUserName());
		model.setUpdatedIP(securityUtil.getRemoteIPAddr());

		try {
			DsepApplicationDtl entity = this.appDtlRepo.findByDadAppId(model.getAppId());
			if (entity != null) {
				entity.setDadAppStatus(value);
				entity.setDadRemarks(model.getRemarks());
				entity.setUpdatedBy(model.getUpdatedBy());
				entity.setUpdatedIP(model.getUpdatedIP());

				this.appDtlRepo.save(entity);

				result = true;
			} else {
				result = false;
				throw new EntityNotFoundException(model.getAppId() + " not found");
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

	public List<ApplicationDtlModel> getApplicationList() {
		List<DsepApplicationDtl> entityList = null;
		try {
			entityList = this.appDtlRepo.findByDadAppStatusNotAndDadDeletedOrderByUpdatedAtDesc(ApplicationStatus.INIT.value(), Boolean.FALSE);

			log.info("Total applications - {}", entityList.size());
		} catch (Exception e) {
			throw e;
		}

		return entityList.stream().map(this.commonBuilder::buildApplicationDtlModel).collect(Collectors.toList());

	}

	public ApplicationDtlModel getDetailsByAppID(String appID) {
		ApplicationDtlModel appDtls = null;

		try {
			DsepApplicationDtl entity = this.appDtlRepo.findByDadAppId(appID);
			if (entity != null) {
				appDtls = this.commonBuilder.buildApplicationDtlModel(entity);
			} else {
				throw new EntityNotFoundException(appID);
			}

		} catch (Exception e) {
			throw e;
		}

		return appDtls;
	}

	public ApplicationDtlModel getDetailsByTxnID(String txnID) {
		ApplicationDtlModel appDtls = null;

		try {
			DsepApplicationDtl entity = this.appDtlRepo.findByDadDsepTxnId(txnID);
			if (entity != null) {
				appDtls = this.commonBuilder.buildApplicationDtlModel(entity);
			} else {
				throw new EntityNotFoundException(txnID);
			}

		} catch (Exception e) {
			throw e;
		}

		return appDtls;
	}
	
	public ApplicationDtlModel getDetailsByID(String appID) {
		ApplicationDtlModel appDtls = null;

		try {
			DsepApplicationDtl entity = this.appDtlRepo.findById(appID).get();
			if (entity != null) {
				appDtls = this.commonBuilder.buildApplicationDtlModel(entity);
			} else {
				throw new EntityNotFoundException(appID + " not found");
			}

		} catch (Exception e) {
			throw e;
		}

		return appDtls;
	}
	
	public List<ApplicationDtlsViewModel> getApplicationViewList() {
		List<ApplicationDtlsView> entityList = null;
		try {
			entityList = this.appDtlViewRepo.findByDadAppStatusNotAndDadDeletedOrderByUpdatedAtDesc(ApplicationStatus.INIT.value(), Boolean.FALSE);

			log.info("Total applications - {}", entityList.size());
		} catch (Exception e) {
			throw e;
		}

		return entityList.stream().map(this.commonBuilder::buildApplicationDtlViewModel).collect(Collectors.toList());

	}

	public ApplicationDtlsViewModel getAppViewDetailsByAppID(String appID) {
		ApplicationDtlsViewModel appDtls = null;

		try {
			ApplicationDtlsView entity = this.appDtlViewRepo.findByDadAppId(appID);
			if (entity != null) {
				appDtls = this.commonBuilder.buildApplicationDtlViewModel(entity);
			} else {
				throw new EntityNotFoundException(appID);
			}

		} catch (Exception e) {
			throw e;
		}

		return appDtls;
	}

	public ApplicationDtlsViewModel getAppViewDetailsByID(String appID) {
		ApplicationDtlsViewModel appDtls = null;

		try {
			ApplicationDtlsView entity = this.appDtlViewRepo.findById(UUID.fromString(appID)).get();
			if (entity != null) {
				appDtls = this.commonBuilder.buildApplicationDtlViewModel(entity);
			} else {
				throw new EntityNotFoundException(appID + " not found");
			}

		} catch (Exception e) {
			throw e;
		}

		return appDtls;
	}
}
