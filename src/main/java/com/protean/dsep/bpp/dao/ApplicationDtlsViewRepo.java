package com.protean.dsep.bpp.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.ApplicationDtlsView;

@Repository
public interface ApplicationDtlsViewRepo extends JpaRepository<ApplicationDtlsView, UUID> {

	List<ApplicationDtlsView> findByDadAppStatusNotAndDadDeletedOrderByUpdatedAtDesc(int status, boolean deleted);

	ApplicationDtlsView findByDadAppId(String appID);

}
