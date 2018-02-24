package com.github.qzagarese.dockerunit.discovery.consul;

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

	@JsonProperty("Address")
	private final String address;
	
	@JsonProperty("ServicePort")
	private final int port;
	
}
