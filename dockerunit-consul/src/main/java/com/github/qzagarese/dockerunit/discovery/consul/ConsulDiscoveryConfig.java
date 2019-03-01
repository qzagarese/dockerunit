package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.qzagarese.dockerunit.annotation.Use;

@Use(service=ConsulDescriptor.class, containerPrefix="consul")
@Use(service=RegistratorDescriptor.class, containerPrefix="registrator")
public class ConsulDiscoveryConfig {

	public static final String DOCKER_BRIDGE_IP_PROPERTY = "docker.bridge.ip";
	public static final String DOCKER_BRIDGE_IP_DEFAULT = "172.17.42.1";
	public static final String DOCKER_HOST_PROPERTY = "docker.host";
	public static final String SERVICE_DISCOVERY_TIMEOUT = "service.discovery.timeout";
	public static final String SERVICE_DISCOVERY_TIMEOUT_DEFAULT = "30";
	public static final String CONSUL_POLLING_PERIOD = "consul.polling.period";
	public static final String CONSUL_POLLING_PERIOD_DEFAULT = "1";
	public static final String CONSUL_DNS_PORT_BRIDGE_BINDING = "consul.dns.port.bridge.binding";
	public static final String CONSUL_DNS_PORT_BRIDGE_BINDING_DEFAULT = "53";
	public static final String CONSUL_DNS_ENABLED_PROPERTY = "consul.dns.enabled";
	public static final String CONSUL_DNS_ENABLED_DEFAULT = "true";

}
