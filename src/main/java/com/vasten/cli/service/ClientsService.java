package com.vasten.cli.service;

import com.vasten.cli.entity.Clients;

/**
 * Service interface for Client related activities
 * 
 * @author scriptuit
 *
 */
public interface ClientsService {

	/**
	 * Create Client
	 * 
	 * @param clientData
	 * @return
	 */
	public Clients createClient(Clients clientData);

}
