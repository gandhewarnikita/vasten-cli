package com.vasten.cli.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private String RESOURCE_ID = "resource_id";

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		System.out.println("in resource configure 1 resources");
		resources.resourceId(RESOURCE_ID).stateless(false);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		System.out.println("in resource configure 2 http");
		http.anonymous().disable()
		.authorizeRequests()
		.antMatchers(HttpMethod.GET, "/api/**").hasRole("ADMIN")
		.antMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
		.antMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
		.antMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
//		.antMatchers(HttpMethod.POST, "**/user").hasRole("ADMIN")
//		.antMatchers(HttpMethod.POST, "**/client").hasRole("ADMIN")
		.and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
	}
}
