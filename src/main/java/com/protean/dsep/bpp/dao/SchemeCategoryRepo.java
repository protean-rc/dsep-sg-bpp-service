package com.protean.dsep.bpp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.DsepSchemeCategory;

@Repository
public interface SchemeCategoryRepo extends JpaRepository<DsepSchemeCategory, Integer> {
	DsepSchemeCategory findByDscCatCode(String code);

}
