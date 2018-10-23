package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBeforeClass extends Statement {

    private static Logger logger = Logger.getLogger(DockerUnitBeforeClass.class.getName());
    
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
		
		
		// Create containers and perform discovery one service at the time
        List<ServiceContext> serviceContexts = descriptor.getDependencies().stream()
            .map(contextBuilder::buildServiceContext)
            .map(ctx -> {
                if (!ctx.allHealthy()) {
                    throw new RuntimeException(ctx.getFormattedErrors());
                }
                logger.info("Performing discovery for service " + ctx.getServices().stream().findFirst().get().getName());
                return discoveryProvider.populateRegistry(ctx);
            })
            .collect(Collectors.toList());  
		
        ServiceContext context = mergeContexts(serviceContexts);

        if(context == null) {
            context = new DefaultServiceContext(new HashSet<>());
        }
        
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
