package com.protean.dsep.bpp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig{

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;

	@Autowired
	private PasswordEncoder encoder;
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) 
	  throws Exception {
	    return http.getSharedObject(AuthenticationManagerBuilder.class)
	      .userDetailsService(userDetailsService)
	      .passwordEncoder(passwordEncoder)
	      .and()
	      .build();
	}

	@Autowired
	public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService)
				.passwordEncoder(this.encoder);
	}

	@Bean
	public JwtAuthenticationFilter authenticationTokenFilterBean() throws Exception {
		return new JwtAuthenticationFilter();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.cors().and().csrf()
	      .disable().addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class)
	      .authorizeRequests()
	      .antMatchers("/api/token", "/api/signup", "/public/submitApplicationDetails", "/api/check", "/api/scheme/provider/add", "/v2/api-docs", "/webjars/**","/swagger-resources/**", "/swagger-ui.html", "/public/**").permitAll()
	      .anyRequest().authenticated()
	      .and()
	      .exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler).and()
	      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	    return http.build();
	}
}
