package com.github.qzagarese.dockerunit.discovery.consul;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDescriptor.CONSUL_DNS_PORT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DNS_POLLING_PERIOD;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DNS_POLLING_PERIOD_DEFAULT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_DEFAULT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_PROPERTY;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.SERVICE_DISCOVERY_TIMEOUT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.SERVICE_DISCOVERY_TIMEOUT_DEFAULT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.docker.DefaultDockerClientProvider;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import io.vertx.core.dns.SrvRecord;

public class ConsulDiscoveryProvider implements DiscoveryProvider {

	private static final String DOCKER_BRIDGE_IP = System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, 
			DOCKER_BRIDGE_IP_DEFAULT);
	private final DnsResolver resolver;
	private final DockerClient dockerClient;
	private final int discoveryTimeout;
	private final int dnsPollingFrequency;
	
	static final String CONSUL_DNS_SUFFIX = ".service.consul";
	
	public ConsulDiscoveryProvider() {
		resolver = new DnsResolver(DOCKER_BRIDGE_IP, CONSUL_DNS_PORT);
		discoveryTimeout = Integer.parseInt(System.getProperty(SERVICE_DISCOVERY_TIMEOUT, 
				SERVICE_DISCOVERY_TIMEOUT_DEFAULT));
		dnsPollingFrequency = Integer.parseInt(System.getProperty(DNS_POLLING_PERIOD, DNS_POLLING_PERIOD_DEFAULT));
		dockerClient = new DefaultDockerClientProvider().getClient();
	}
	
	@Override
	public Class<?> getDiscoveryConfig() {
		return ConsulDiscoveryConfig.class;
	}

	@Override
	public ServiceContext populateRegistry(ServiceContext context) {
		Set<Service> services = context.getServices().stream()
				.map(s -> doDiscovery(s))
				.collect(Collectors.toSet());
		return new DefaultServiceContext(services);
	}

	@Override
	public ServiceContext clearRegistry(ServiceContext currentContext, ServiceContext globalContext) {
		Set<Service> services = currentContext.getServices().stream()
				.map(s -> doCleanup(s, globalContext.getService(s.getName())))
				.collect(Collectors.toSet());
		return new DefaultServiceContext(services);
	}

	private Service doDiscovery(Service s) {
		List<SrvRecord> records;
		try {
			records = resolver.resolveSRV(s.getName() + CONSUL_DNS_SUFFIX, s.getInstances().size(), 
					discoveryTimeout, dnsPollingFrequency);
		} catch (Throwable t) {
			return s.withInstances(s.getInstances().stream()
					.map(i -> i.withStatus(Status.ABORTED)
							.withStatusDetails(t.getMessage()))
					.collect(Collectors.toSet()));
		}
		
		Set<ServiceInstance> withPorts = s.getInstances().stream()
				.map(si -> {
					InspectContainerResponse r = dockerClient.inspectContainerCmd(si.getContainerId()).exec();
					return si.withPort(findPort(r, records))
							.withIp(DOCKER_BRIDGE_IP)
							.withStatus(Status.DISCOVERED)
							.withStatusDetails("Discovered via consul + registrator");					
				}).collect(Collectors.toSet());
		
		return s.withInstances(withPorts);
	}

	private Service doCleanup(Service current, Service global) {
		try {
			int expectedRecords = global != null? global.getInstances().size() : 0;
			resolver.verifyCleanup(current.getName() + CONSUL_DNS_SUFFIX, expectedRecords, discoveryTimeout, dnsPollingFrequency);
			return current;
		} catch (Throwable t) {
			return current.withInstances(current.getInstances().stream()
				.map(si -> si.withStatus(Status.TERMINATION_FAILED)
						.withStatusDetails(t.getMessage()))
				.collect(Collectors.toSet()));
		}
	}

	private int findPort(InspectContainerResponse response, List<SrvRecord> records) {
		SrvRecord record = records.stream()
			.filter(r -> this.matchPort(r, response))
			.findFirst()
			.orElseThrow(() ->  
				new RuntimeException("Cannot find exposed port/ip for container " + response.getName()));
		return record.port();
	}

	private boolean matchPort(SrvRecord record, InspectContainerResponse r) {
		Optional<Binding[]> opt = r.getNetworkSettings().getPorts().getBindings()
			.values().stream()
			.filter(b -> b.length > 0 
					&& isInt(b[0].getHostPortSpec()) 
					&& record.port() == Integer.parseInt(b[0].getHostPortSpec()))
			.findFirst();	
		return opt.isPresent();
	}

	private boolean isInt(String hostPortSpec) {
		try {
			Integer.parseInt(hostPortSpec);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

}
