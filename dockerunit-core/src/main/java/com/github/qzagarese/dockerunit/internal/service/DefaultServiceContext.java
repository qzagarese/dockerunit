package com.github.qzagarese.dockerunit.internal.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;

public class DefaultServiceContext implements ServiceContext {

    private final Map<String, Service> services = new HashMap<>();
    
    public DefaultServiceContext(Set<Service> services) {
        services.forEach(s ->  this.services.put(s.getName(), s));
    }
    
    @Override
    public Set<Service> getServices() {
        return services.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Service getService(String name) {
        return services.get(name);
    }

	@Override
	public ServiceContext merge(ServiceContext context) {
		DefaultServiceContext clone = new DefaultServiceContext(new HashSet<>(services.values()));

		if(context != null) {
			Set<Service> servicesSet = clone.getServices().stream()
					.map(s -> {
						Service s2 = context.getService(s.getName());
						if(s2 == null) {
							return s;
						} else {
							Set<ServiceInstance> augmented = s.getInstances();
							augmented.addAll(s2.getInstances());
							return s.withInstances(augmented);
						}
					}).collect(Collectors.toSet());
			Set<Service> toAdd = context.getServices().stream()
					.filter(s -> clone.getService(s.getName()) == null)
					.collect(Collectors.toSet());
			servicesSet.addAll(toAdd);
			return new DefaultServiceContext(servicesSet);
		}
		return clone;
	}

	@Override
	public boolean allHealthy() {
		return services.isEmpty() ||
				services.values().stream()
				.map(s -> s.isHealthy())
				.reduce((b1, b2) -> b1 && b2)
				.get();
	}

	@Override
    public boolean checkStatus(Status status) {
	    return services.isEmpty() ||
                services.values().stream()
                .map(s -> s.checkStatus(status))
                .reduce((b1, b2) -> b1 && b2)
                .get();
    }

    @Override
	public String getFormattedErrors() {
		return services.values().stream()
			.map(s -> "\nService: " + s.getName() 
				+ "\nErrors:\n\t" + 
				s.getWarnings().stream()
					.reduce((w1, w2) -> w1 + "\n\t" + w2)
					.orElseGet(() -> "")
			).reduce((s1, s2) -> s1 + "\n" + s2)
			.get();
	}

	@Override
	public ServiceContext subtract(ServiceContext context) {
		DefaultServiceContext clone = new DefaultServiceContext(new HashSet<>(services.values()));
		if(context != null) {
			Set<Service> servicesSet = clone.getServices().stream()
					.map(s -> {
						Service s2 = context.getService(s.getName());
						if(s2 == null) {
							return s;
						} else {
							Set<ServiceInstance> remaining = new HashSet<>(s.getInstances());
							remaining.removeAll(s2.getInstances());
							return remaining.size() > 0 ? s.withInstances(remaining) : null;
						}
					})
					.filter(s -> s != null)
					.collect(Collectors.toSet());
			return new DefaultServiceContext(servicesSet);
		}
		return clone;
	}
}
