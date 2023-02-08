package com.protean.dsep.bpp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.protean.dsep.bpp.constant.ApplicationConstant;
import com.protean.dsep.bpp.constant.InternalConstant;
import com.protean.dsep.bpp.entity.DsepScheme;
import com.protean.dsep.bpp.entity.DsepSchemeProvider;
import com.protean.dsep.bpp.entity.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BppDao {

	@Autowired
	UserEntityRepo userEntityRepo;
	
	@Autowired
	SchemeRepo schemeRepo;
	
	@Autowired
	SchemeProviderRepo schemeProviderRepo;
	
	public Map<String, UserEntity> getSchemeProviderList() {
		List<UserEntity> list = (List<UserEntity>) userEntityRepo.findAll();
		Map<String, UserEntity> map = new HashMap<>();

		for (UserEntity entity : list) {
			map.put(entity.getUserId(), entity);
		}

		log.info("the scholarships & grants providers map is {}", map);
		return map;
	}

	public Map<String, DsepSchemeProvider> getSchemeProviders() {
		List<DsepSchemeProvider> list = (List<DsepSchemeProvider>) schemeProviderRepo.findAll();
		Map<String, DsepSchemeProvider> map = new HashMap<>();

		for (DsepSchemeProvider entity : list) {
			map.put(entity.getDspSchemeProviderId(), entity);
		}

		log.info("the scholarships & grants providers map is {}", map);
		return map;
	}
	
	public List<DsepScheme> getSchemes(String intent) {
		List<DsepScheme> list = schemeRepo.findBySearchIntent(ApplicationConstant.PERCENT.concat(intent).concat(ApplicationConstant.PERCENT));
				
		log.info("the size of intent list is {}", list.size());
		return list;
	}

	//public Map<String, List<DsepScheme>> getSchemesGroupBySchemeProviders(String intent) {
	public Map<String, List<DsepScheme>> getSchemesGroupBySchemeProviders(String intent, String intentType) {
		//List<DsepScheme> list = schemeRepo.findBySearchIntent(ApplicationConstant.PERCENT.concat(intent).concat(ApplicationConstant.PERCENT));
		//List<DsepScheme> list = schemeRepo.findByGenderCriteria("Female");
		//List<DsepScheme> list = schemeRepo.findByFamilyIncomeCriteria(1000000);
	
		List<DsepScheme> list = new ArrayList<DsepScheme>();
		if(intentType.equalsIgnoreCase(InternalConstant.INTENT_TYPE_SCHEME_NAME)) {
			list = schemeRepo.findBySearchIntent(ApplicationConstant.PERCENT.concat(intent).concat(ApplicationConstant.PERCENT));
			
		}else if(intentType.equalsIgnoreCase(InternalConstant.INTENT_TYPE_GENDER)) {
			list = schemeRepo.findByGenderCriteria(intent);
			
		}else if(intentType.equalsIgnoreCase(InternalConstant.INTENT_TYPE_MAX_FAMILIY_INCOME)) {
			list = schemeRepo.findByFamilyIncomeCriteria(Integer.valueOf(intent));
			
		}else if(intentType.equalsIgnoreCase(InternalConstant.INTENT_TYPE_CASTE)) {
			list = schemeRepo.findByCasteCriteria(ApplicationConstant.PERCENT.concat(intent).concat(ApplicationConstant.PERCENT));
			
		}else if(intentType.equalsIgnoreCase(InternalConstant.INTENT_TYPE_GENDER_COURSE_CAT)) {
			String[] cat = intent.split("\\|");
			String gender = cat[0];
			String courseCat = cat[1];
			list = schemeRepo.findByGenderCriteriaAndCourseCategory(gender, courseCat);
			
		}
		
		log.info("the size of intent list is {}", list.size());

		Map<String, List<DsepScheme>> sellerGroup = list.stream()
				.collect(Collectors.groupingBy(DsepScheme::getDsSchemeProviderId));

		return sellerGroup;
	}

}
