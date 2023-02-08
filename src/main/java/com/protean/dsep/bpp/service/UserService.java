package com.protean.dsep.bpp.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.protean.dsep.bpp.dao.UserEntityRepo;
import com.protean.dsep.bpp.dao.UserRolesEntityRepo;
import com.protean.dsep.bpp.entity.UserEntity;
import com.protean.dsep.bpp.entity.UserRolesEntity;
import com.protean.dsep.bpp.exception.UserAlreadyRegisteredException;
import com.protean.dsep.bpp.model.UserModel;
import com.protean.dsep.bpp.model.UserRoleModel;



@Service
public class UserService {

	@Autowired
	private UserEntityRepo userEntityRepo;
	
	@Autowired
	private UserRolesEntityRepo userRolesEntityRepo;

	@Autowired
	private BCryptPasswordEncoder bcryptEncoder;

	public void delete(String id) {
		this.userEntityRepo.deleteById(id);
	}

	public UserModel findById(String id) {
		UserEntity entity = this.userEntityRepo.findById(id).orElse(new UserEntity());
		return buildModel(entity);
	}

	public boolean save(UserModel model) throws UserAlreadyRegisteredException {
		UserEntity entity = buildEntity(model);
		boolean isPresent = this.userEntityRepo.existsById(model.getUserId());
		if (isPresent) {
			throw new UserAlreadyRegisteredException("UserId already registered: " + model.getUserId());
		}
		this.userEntityRepo.save(entity);
		return true;
	}

	private UserEntity buildEntity(UserModel model) {
		UserEntity entity = new UserEntity();

		entity.setUserId(model.getUserId());
		entity.setPassword(this.bcryptEncoder.encode(model.getPassword()));
		entity.setFullName(model.getFullName());
		entity.setLocation(model.getLocation());
		entity.setProviderId(model.getProviderId());
		entity.setCreatedBy(model.getCreatedBy());
		entity.setCreatedIP(model.getCreatedIP());
		entity.setUpdatedBy(model.getUpdatedBy());
		entity.setUpdatedIP(model.getUpdatedBy());
		entity.setUserRoles(buildRoles(model.getUserId()));

		return entity;
	}

	private List<UserRolesEntity> buildRoles(String userId) {
		UserRolesEntity rolesEntity = new UserRolesEntity();
		rolesEntity.getPk().setRole("ADMIN");
		rolesEntity.getPk().setUserId(userId);
		rolesEntity.setCreatedBy("SYSTEM");
		rolesEntity.setCreatedIP("0.0.0.0.0.0.0.0");
		return Arrays.asList(rolesEntity);
	}

	private UserModel buildModel(UserEntity entity) {
		UserModel model = new UserModel();

		model.setUserId(entity.getUserId());
		model.setFullName(entity.getFullName());
		model.setLocation(entity.getLocation());
		model.setProviderId(entity.getProviderId());
		model.setCreatedAt(entity.getCreatedAt().toString());
		model.setCreatedBy(entity.getCreatedBy());
		model.setCreatedIP(entity.getCreatedIP());
		model.setUpdatedAt(entity.getUpdatedAt().toString());
		model.setUpdatedBy(entity.getUpdatedBy());
		model.setUpdatedIP(entity.getUpdatedBy());
		return model;
	}

	public void assignRole(UserRoleModel role) {
		UserRolesEntity rolesEntity = new UserRolesEntity();
		rolesEntity.getPk().setUserId(role.getEmailId());
		rolesEntity.getPk().setRole(role.getRole());
		rolesEntity.setCreatedBy(role.getCreatedBy());
		rolesEntity.setCreatedIP(role.getCreatedIP());
		rolesEntity.setUpdatedBy(role.getUpdatedBy());
		rolesEntity.setUpdatedIP(role.getUpdatedBy());
		this.userRolesEntityRepo.save(rolesEntity);
	}

}
