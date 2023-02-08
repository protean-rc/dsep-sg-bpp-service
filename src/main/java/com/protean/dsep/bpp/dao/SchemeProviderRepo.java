package com.protean.dsep.bpp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.DsepSchemeProvider;

@Repository
public interface SchemeProviderRepo extends JpaRepository<DsepSchemeProvider, String> {

	List<DsepSchemeProvider> findByDspIsActive(boolean activeFlag);
	
	List<DsepSchemeProvider> findByDspIsDeleted(boolean deleteFlag);
	
	DsepSchemeProvider findByDspSchemeProviderId(String spID);
}
