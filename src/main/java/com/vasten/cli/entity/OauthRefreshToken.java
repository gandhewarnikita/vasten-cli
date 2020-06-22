package com.vasten.cli.entity;

import java.sql.Blob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class for Refresh Token data
 * 
 * @author scriptuit
 *
 */
@Entity
@Table(name = "oauth_refresh_token")
public class OauthRefreshToken {

	@Id
	@Column(name = "token_id")
	private String token_id;
	
	@Column(name = "token")
	private Blob token;
	
	@Column(name = "authentication")
	private Blob authentication;

	public String getToken_id() {
		return token_id;
	}

	public void setToken_id(String token_id) {
		this.token_id = token_id;
	}

	public Blob getToken() {
		return token;
	}

	public void setToken(Blob token) {
		this.token = token;
	}

	public Blob getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Blob authentication) {
		this.authentication = authentication;
	}

}