package com.protean.dsep.bpp.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.DsepScheme;

@Repository
public interface SchemeRepo extends JpaRepository<DsepScheme, UUID> {

	List<DsepScheme> findByDsSchemeProviderId(String userID);

	List<DsepScheme> findByDsSchemeProviderIdAndDsIsDeleted(String userID, boolean delFlag);
	
	List<DsepScheme> findByDsSchemeProviderIdAndDsIsDeletedOrderByUpdatedAtDesc(String providerID, boolean delFlag);
	
	@Query("from DsepScheme where (lower(dsSchemeName) like lower(:intent) or lower(dsSchemeDescription) like lower(:intent) or lower(dsSchemeType) like lower(:intent)) and dsIsDeleted=false")
	List<DsepScheme> findBySearchIntent(String intent);
	
	DsepScheme findByDsSchemeId(String schemeID);
	
	DsepScheme findByDsSchemeIdAndDsSchemeProviderId(String schemeID, String providerID);
	
	List<DsepScheme> findAllByOrderByUpdatedAtDesc();
	
	@Query(
		value="select * from dsep_schemes ds where lower(ds.ds_eligibility->> 'gender') = lower(?1) and ds.ds_is_published=true and ds.ds_is_deleted=false",
		nativeQuery = true)
	List<DsepScheme> findByGenderCriteria(String intent);

	@Query(
			value="select * from dsep_schemes ds where lower(ds.ds_eligibility->> 'gender') = lower(?1) and ds.ds_scheme_for IN (?2) and ds.ds_is_published=true and ds.ds_is_deleted=false",
			nativeQuery = true)
		List<DsepScheme> findByGenderCriteriaAndCourseCategory(String gender, String courseCategory);
	
	@Query(
		value="select * from dsep_schemes ds where lower(ds_eligibility->>'caste') like lower(?1) and ds_is_published=true and ds_is_deleted=false",
		nativeQuery = true)
	List<DsepScheme> findByCasteCriteria(String intent);
	
	@Query(
		value="select * from public.dsep_schemes ds where cast((ds_eligibility->>'familyIncome') as numeric) >= ?1 and ds_is_published=true and ds_is_deleted=false",
		nativeQuery = true)
	List<DsepScheme> findByFamilyIncomeCriteria(int income);
	
	List<DsepScheme> findByDsSchemeForAndDsIsDeleted(String schemeCat, boolean delFlag);
}
