package com.protean.dsep.bpp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.UserRolesEntity;
import com.protean.dsep.bpp.entity.UserRolesEntity.UserRolesEntityPk;

@Repository
public interface UserRolesEntityRepo extends JpaRepository<UserRolesEntity, UserRolesEntityPk>{

}
