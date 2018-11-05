package com.github.qzagarese.dockerunit.internal.service;

import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceBuilderFactory;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.docker.DockerClientProviderFactory;


public class DefaultServiceContextBuilder implements ServiceContextBuilder {

    private final DockerClient client = DockerClientProviderFactory.create().getClient();
    private final ServiceBuilder serviceBuilder = ServiceBuilderFactory.create();
    
    @Override
    public ServiceContext buildContext(DependencyDescriptor descriptor) {
    	return new DefaultServiceContext(descriptor.getDependencies().stream()
                .map(d -> serviceBuilder.build(d, client))
                .collect(Collectors.toSet()));
    }

	@Override
	public ServiceContext clearContext(ServiceContext context) {
		return new DefaultServiceContext(context.getServices().stream()
				.map(s -> serviceBuilder.cleanup(s, client))
				.collect(Collectors.toSet()));
	}

}
