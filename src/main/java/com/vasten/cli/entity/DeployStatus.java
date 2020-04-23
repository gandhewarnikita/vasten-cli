package com.vasten.cli.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "deploystatus")
public class DeployStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "deployment_name")
	private String name;

	@Column(name = "deployment_status")
	@Enumerated(EnumType.STRING)
	private DeploymentStatus status;
	
//	@Column(name = "instance_name")
//	private String instanceName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	@Override
	public String toString() {
		return "DeployStatus [id=" + id + ", name=" + name + ", status=" + status + "]";
	}

}
