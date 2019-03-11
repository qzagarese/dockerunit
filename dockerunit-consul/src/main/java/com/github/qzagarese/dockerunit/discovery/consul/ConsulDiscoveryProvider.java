package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.EnableConsul;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;
import com.github.qzagarese.dockerunit.internal.docker.DefaultDockerClientProvider;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDescriptor.CONSUL_PORT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.*;

public class ConsulDiscoveryProvider implements DiscoveryProvider {

	private static final String DOCKER_HOST = System.getProperty(DOCKER_HOST_PROPERTY, 
			System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT));
	private final ConsulHttpResolver resolver;
	private final DockerClient dockerClient;
	private final int discoveryTimeout;
	private final int consulPollingPeriod;
	
	static final String CONSUL_DNS_SUFFIX = ".service.consul";
	
	public ConsulDiscoveryProvider() {
		resolver = new ConsulHttpResolver(DOCKER_HOST, CONSUL_PORT);
		discoveryTimeout = Integer.parseInt(System.getProperty(SERVICE_DISCOVERY_TIMEOUT, 
				SERVICE_DISCOVERY_TIMEOUT_DEFAULT));
		consulPollingPeriod = Integer.parseInt(System.getProperty(CONSUL_POLLING_PERIOD, CONSUL_POLLING_PERIOD_DEFAULT));
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
		List<ServiceRecord> records;
		try {
			records = resolver.resolveService(s.getName(), s.getInstances().size(), 
					discoveryTimeout, consulPollingPeriod, extractInitialDelay(s.getDescriptor()));
		} catch (Exception e) {
			return s.withInstances(s.getInstances().stream()
					.map(i -> i.withStatus(Status.ABORTED)
							.withStatusDetails(e.getMessage()))
					.collect(Collectors.toSet()));
		}
		
		Set<ServiceInstance> withPorts = s.getInstances().stream()
				.map(si -> {
					InspectContainerResponse r = dockerClient.inspectContainerCmd(si.getContainerId()).exec();
					return si.withPort(findPort(r, records).orElse(0))
							.withIp(DOCKER_HOST)
							.withStatus(Status.DISCOVERED)
							.withStatusDetails("Discovered via consul + registrator");					
				}).collect(Collectors.toSet());
		
		return s.withInstances(withPorts);
	}

	private int extractInitialDelay(ServiceDescriptor descriptor) {
	    return descriptor.getOptions().stream()
	        .filter(EnableConsul.class::isInstance)
	        .findFirst()
	        .map(EnableConsul.class::cast)
	        .map(EnableConsul::initialDelay)
	        .orElse(0);
    }

    private Service doCleanup(Service current, Service global) {
		try {
			int expectedRecords = global != null? global.getInstances().size() : 0;
			resolver.verifyCleanup(current.getName() + CONSUL_DNS_SUFFIX, expectedRecords, discoveryTimeout, consulPollingPeriod);
			return current;
		} catch (Exception e) {
			return current.withInstances(current.getInstances().stream()
				.map(si -> si.withStatus(Status.TERMINATION_FAILED)
						.withStatusDetails(e.getMessage()))
				.collect(Collectors.toSet()));
		}
	}

	private Optional<Integer> findPort(InspectContainerResponse response, List<ServiceRecord> records) {
		return records.stream()
			.filter(r -> matchRecord(r, response))
			.findFirst()
			.map(r -> ContainerUtils.extractMappedPort(r.getPort(), response.getNetworkSettings()))
			.get();

	}

	private boolean matchRecord(ServiceRecord record, InspectContainerResponse r) {
		return matchIP(record.getServiceAddress(), r) && matchPort(record.getPort(), r);
	}

	private boolean matchPort(int port, InspectContainerResponse r) {
		return r.getNetworkSettings().getPorts().getBindings().entrySet().stream()
				.map(entry -> entry.getKey().getPort())
				.filter(p -> p == port)
				.findFirst()
				.isPresent();
	}

	private boolean matchIP(String address, InspectContainerResponse r) {
		return null != address && address.equals(Optional.ofNullable(ContainerUtils.extractBridgeIpAddress(r.getNetworkSettings()).orElse(null)));
	}
	
}
