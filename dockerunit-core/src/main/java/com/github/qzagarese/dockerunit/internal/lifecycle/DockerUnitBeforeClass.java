package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

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
		if(!discoveryContext.allHealthy()) {
			throw new RuntimeException(discoveryContext.getFormattedErrors());
		}
		
		
		List<ServiceContext> serviceContexts = new ArrayList<>();
        
        descriptor.getDependencies().forEach(svc -> {
            // Build containers
            ServiceContext singleSvcContext = contextBuilder.buildServiceContext(svc);
            if(!singleSvcContext.allHealthy()) {
                throw new RuntimeException(singleSvcContext.getFormattedErrors());
            }
           
            // Perform discovery
            singleSvcContext = discoveryProvider.populateRegistry(singleSvcContext);
            if(!singleSvcContext.allHealthy()) {
                throw new RuntimeException(singleSvcContext.getFormattedErrors());
            }
            serviceContexts.add(singleSvcContext);
        });
		
        ServiceContext context = mergeContexts(serviceContexts);

		runner.setClassContext(context);
		if(!context.allHealthy()) {
			throw new RuntimeException(context.getFormattedErrors());
		}
        next.evaluate();
	}

	private ServiceContext mergeContexts(List<ServiceContext> serviceContexts) {
        ServiceContext completeContext = null;
        if (serviceContexts.size() > 0) {
            completeContext = serviceContexts.remove(0);
        }
        for (ServiceContext serviceContext : serviceContexts) {
            completeContext = completeContext.merge(serviceContext);
        }
        return completeContext;
    }
	
	
}
