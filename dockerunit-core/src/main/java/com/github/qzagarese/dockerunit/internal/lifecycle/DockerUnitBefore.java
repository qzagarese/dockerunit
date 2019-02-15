package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;

import com.github.qzagarese.dockerunit.NetworkContext;
import com.github.qzagarese.dockerunit.internal.NetworkContextBuider;
import com.github.qzagarese.dockerunit.internal.network.DefaultNetworkContext;
import org.junit.runners.model.FrameworkMethod;
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
public class DockerUnitBefore extends Statement {

	private final FrameworkMethod method;
	private final DockerUnitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder serviceContextBuilder;
	private final NetworkContextBuider networkContextBuider;
	private final UsageDescriptor descriptor;
	
	@Override
	public void evaluate() throws Throwable {

		DockerUnitSetup dockerUnitSetup = new DockerUnitSetup(networkContextBuider, serviceContextBuilder, discoveryProvider);
		NetworkContext methodLevelNetworkContext = dockerUnitSetup.setupNetworks(descriptor);
		ServiceContext methodLevelServiceContext = dockerUnitSetup.setupServices(descriptor);

		if(methodLevelNetworkContext == null) {
			methodLevelNetworkContext = new DefaultNetworkContext();
		}

	    if (methodLevelServiceContext == null) {
	        methodLevelServiceContext = new DefaultServiceContext(new HashSet<>());
	    }


	    methodLevelServiceContext = methodLevelServiceContext.merge(runner.getClassContext());


	    runner.setNetworkContext(method, methodLevelNetworkContext);
	    runner.setContext(method, methodLevelServiceContext);
        if (!methodLevelServiceContext.checkStatus(Status.DISCOVERED)) {
        	throw new RuntimeException(methodLevelServiceContext.getFormattedErrors());
        }
        next.evaluate();
	}
	
}
