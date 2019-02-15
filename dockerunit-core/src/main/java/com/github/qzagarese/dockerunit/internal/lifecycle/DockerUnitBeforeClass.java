package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;

import com.github.qzagarese.dockerunit.NetworkContext;
import com.github.qzagarese.dockerunit.internal.NetworkContextBuider;
import com.github.qzagarese.dockerunit.internal.network.DefaultNetworkContext;
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
	private final ServiceContextBuilder serviceContextBuilder;
	private final NetworkContextBuider networkContextBuider;
	private final UsageDescriptor descriptor;
	private final UsageDescriptor discoveryProviderDescriptor;
	
	@Override
	public void evaluate() throws Throwable {
		ServiceContext discoveryContext = serviceContextBuilder.buildContext(discoveryProviderDescriptor);
		runner.setDiscoveryContext(discoveryContext);
		if (!discoveryContext.checkStatus(Status.STARTED)) {
			throw new RuntimeException(discoveryContext.getFormattedErrors());
		}

		DockerUnitSetup dockerUnitSetup = new DockerUnitSetup(networkContextBuider, serviceContextBuilder, discoveryProvider);
		NetworkContext networkContext = dockerUnitSetup.setupNetworks(descriptor);
		ServiceContext serviceContext = dockerUnitSetup.setupServices(descriptor);


		if(networkContext == null) {
			networkContext = new DefaultNetworkContext();
		}
        if (serviceContext == null) {
            serviceContext = new DefaultServiceContext(new HashSet<>());
        }

        runner.setClassNetworkContext(networkContext);
		runner.setClassContext(serviceContext);
		if (!serviceContext.checkStatus(Status.DISCOVERED)) {
			throw new RuntimeException(serviceContext.getFormattedErrors());
		}
        next.evaluate();
	}
	
}
