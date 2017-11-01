package com.github.qzagarese.dockerunit.internal.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;

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
		return doMerge(context, false);
	}

	@Override
	public ServiceContext mergeInstances(ServiceContext context) {
		return doMerge(context, true);
	}

	@Override
	public boolean allHealthy() {
		return services.values().stream()
				.map(s -> s.isHealthy())
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

	private ServiceContext doMerge(ServiceContext context, boolean commonSvcsOnly) {
		DefaultServiceContext clone = new DefaultServiceContext(new HashSet<>(services.values()));
		if(context != null) {
			Set<Service> servicesList = context.getServices().stream()
				.filter(s -> clone.getService(s.getName()) != null || !commonSvcsOnly) 	
				.map(s -> {
					Service s2 = clone.getService(s.getName());
					if(s2 != null) {
						Set<ServiceInstance> instances = new HashSet<>(s2.getInstances());
						instances.addAll(s.getInstances());
						return s2.withInstances(instances);
					} else {
						return s;
					}
				}).collect(Collectors.toSet());
			return new DefaultServiceContext(servicesList);
		}
		return clone;
	}

	@Override
	public ServiceContext subtract(ServiceContext context) {
		DefaultServiceContext clone = new DefaultServiceContext(new HashSet<>(services.values()));
		if(context != null) {
			Set<Service> servicesList = clone.getServices().stream()
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
			return new DefaultServiceContext(servicesList);
		}
		return clone;
	}
}
