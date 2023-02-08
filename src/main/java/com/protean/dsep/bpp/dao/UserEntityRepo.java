package com.protean.dsep.bpp.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.protean.dsep.bpp.entity.UserEntity;

public interface UserEntityRepo extends JpaRepository<UserEntity, String> {
	UserEntity findByUserId(String userId);
}
