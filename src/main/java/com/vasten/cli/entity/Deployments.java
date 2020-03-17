package com.vasten.cli.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "deployments")
public class Deployments {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "client_id")
	private Clients clients;

	@Column(name = "name")
	private String name;

	@Column(name = "status")
	private DeploymentStatus status;

	@Column(name = "prefix")
	private String prefix;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Clients getClients() {
		return clients;
	}

	public void setClients(Clients clients) {
		this.clients = clients;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return "Deployments [id=" + id + ", clients=" + clients + ", name=" + name + ", status=" + status
				+ ", prefix=" + prefix + "]";
	}

}
