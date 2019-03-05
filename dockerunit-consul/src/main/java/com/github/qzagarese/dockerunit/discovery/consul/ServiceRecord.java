package com.github.qzagarese.dockerunit.discovery.consul;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@Wither
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRecord {

	@JsonProperty("ServiceName")
	private final String name;
	
	@JsonProperty("Address")
	private final String address;
	
	@JsonProperty("ServicePort")
	private final int port;

	@JsonProperty("ServiceAddress")
	private String serviceAddress;

	@JsonProperty("Service")
	private final Service service;
	
	@JsonProperty("Checks")
	private final List<Check> checks;
	
	
	public String getName() {
		return service != null ? service.getName() : name; 
	}
	
	public String getAddress() {
		return service != null ? service.getAddress() : address;
	}
	
	public int getPort() {
		return service != null ? service.getPort() : port;
	}

	@Wither
	@Getter
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Service {
		
		@JsonProperty("Service")
		private final String name;
		
		@JsonProperty("Address")
		private final String address;
		
		@JsonProperty("Port")
		private final int port;
		
	}
	
	@Wither
	@Getter
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Check {
		
		public static final String PASSING = "passing";

		@JsonProperty("Name")
		private final String name;
		
		@JsonProperty("Status")
		private final String status;
		
	}
}
