package com.protean.dsep.bpp.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.protean.dsep.bpp.dao.UserEntityRepo;
import com.protean.dsep.bpp.entity.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SecurityService implements UserDetailsService {

	@Autowired
	private UserEntityRepo userEntityRepo;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		UserEntity entity = this.userEntityRepo.findByUserId(userId);
		log.info("UserEntity is {}", entity);

		if (entity == null) {
			throw new UsernameNotFoundException("Invalid userID or password.");
		}
		
		return new User(entity.getUserId(), entity.getPassword(), getAuthority(entity));
	}

	private Set<SimpleGrantedAuthority> getAuthority(UserEntity entity) {
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		entity.getUserRoles().forEach(role -> {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getPk().getRole()));
		});
		return authorities;
	}

}
