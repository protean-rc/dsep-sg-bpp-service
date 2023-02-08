package com.protean.dsep.bpp.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.protean.dsep.bpp.entity.UserRolesEntity;

@Repository
public class UserDao {

	@PersistenceContext
	private EntityManager em;

	public void saveRole(UserRolesEntity entity) {
		this.em.persist(entity);
	}
}
