package com.github.qzagarese.dockerunit.internal.lifecycle;

import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBeforeClass extends Statement {

	private final DockerUnitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	private final DependencyDescriptor descriptor;
	private final DependencyDescriptor discoveryProviderDescriptor;
	
	@Override
	public void evaluate() throws Throwable {
		ServiceContext discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
		runner.setDiscoveryContext(discoveryContext);
		if(!discoveryContext.allHealthy()) {
			throw new RuntimeException(discoveryContext.getFormattedErrors());
		}
		ServiceContext context = contextBuilder.buildContext(descriptor);
		runner.setClassContext(context);
		if(!context.allHealthy()) {
			throw new RuntimeException(context.getFormattedErrors());
		}
		context = discoveryProvider.populateRegistry(context);
		runner.setClassContext(context);
		if(!context.allHealthy()) {
			throw new RuntimeException(context.getFormattedErrors());
		}
        next.evaluate();
	}

}
