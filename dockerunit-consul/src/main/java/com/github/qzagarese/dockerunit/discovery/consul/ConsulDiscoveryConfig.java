package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.qzagarese.dockerunit.annotation.Use;

@Use(service=ConsulDescriptor.class, containerPrefix="consul")
@Use(service=RegistratorDescriptor.class, containerPrefix="registrator")
public class ConsulDiscoveryConfig {

	static final String DOCKER_BRIDGE_IP_PROPERTY = "docker.bridge.ip";
	static final String DOCKER_BRIDGE_IP_DEFAULT = "172.17.42.1";
	static final String DOCKER_HOST_PROPERTY = "docker.host";
	static final String SERVICE_DISCOVERY_TIMEOUT = "service.discovery.timeout";
	static final String SERVICE_DISCOVERY_TIMEOUT_DEFAULT = "30";
	static final String CONSUL_POLLING_PERIOD = "consul.polling.period";
	static final String CONSUL_POLLING_PERIOD_DEFAULT = "1";
	
	
}
