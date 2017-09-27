package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Entity that represents an ElasTest service.")
public class SupportService {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("shortName")
	private String shortName;
	
	public SupportService(){
		
	}
	
	public SupportService(String id, String name, String shortName) {
		super();
		this.id = id;
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
