package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;

import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBeforeClass extends Statement {

	private final DockerUnitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	private final UsageDescriptor descriptor;
	private final UsageDescriptor discoveryProviderDescriptor;
	
	@Override
	public void evaluate() throws Throwable {
		ServiceContext discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
		runner.setDiscoveryContext(discoveryContext);
		if (!discoveryContext.checkStatus(Status.STARTED)) {
			throw new RuntimeException(discoveryContext.getFormattedErrors());
		}
		
		ServiceContext context = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);

        if (context == null) {
            context = new DefaultServiceContext(new HashSet<>());
        }
        
		runner.setClassContext(context);
		if (!context.checkStatus(Status.DISCOVERED)) {
			throw new RuntimeException(context.getFormattedErrors());
		}
        next.evaluate();
	}
	
}
