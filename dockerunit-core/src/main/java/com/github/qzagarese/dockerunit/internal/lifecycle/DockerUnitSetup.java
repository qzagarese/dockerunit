package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DockerUnitSetup {

	private static final Logger logger = Logger.getLogger(DockerUnitSetup.class.getSimpleName());
	
	private final ServiceContextBuilder contextBuilder;
	private final DiscoveryProvider discoveryProvider;

	public ServiceContext setup(UsageDescriptor descriptor) {
		// Create containers and perform discovery one service at the time
		final AtomicBoolean failureOccured = new AtomicBoolean(false);
        List<ServiceContext> serviceContexts = descriptor.getDependencies().stream()
            .map(contextBuilder::buildServiceContext)
            .map(ctx -> {
                if (!ctx.checkStatus(Status.STARTED)) {
                	failureOccured.set(true);
                    return abortService(ctx);
                }
                return ctx;
            }).map(ctx -> {
                if(failureOccured.get()) {
                	logger.info("Skipping discovery of service " + getServiceName(ctx) + " due to a previous failure.");
                	return abortService(ctx);
                } 
            
                logger.info("Performing discovery for service " + getServiceName(ctx));
                ServiceContext postDiscoveryCtx = discoveryProvider.populateRegistry(ctx);
                if (!ctx.checkStatus(Status.DISCOVERED)) {
                	failureOccured.set(true);
                }
                return postDiscoveryCtx;
            }).collect(Collectors.toList());  
        
        ServiceContext completeContext = mergeContexts(serviceContexts);
		return completeContext;
	}

	private String getServiceName(ServiceContext ctx) {
		return ctx.getServices().stream().findFirst().get().getName();
	}

    private ServiceContext abortService(ServiceContext ctx) {
    	return new DefaultServiceContext(ctx.getServices().stream()
    		.map(svc -> svc.withInstances(abortInstances(svc)))
    		.collect(Collectors.toSet()));
    }

	private Set<ServiceInstance> abortInstances(Service svc) {
		return svc.getInstances().stream()
			.map(si -> si.withStatus(Status.ABORTED)
					.withStatusDetails("Aborted due to previous failure."))
		.collect(Collectors.toSet());
	}
    
    private ServiceContext mergeContexts(List<ServiceContext> serviceContexts) {
        ServiceContext completeContext = null;
        if (!serviceContexts.isEmpty()) {
            completeContext = serviceContexts.remove(0);
        }
        for (ServiceContext serviceContext : serviceContexts) {
            completeContext = completeContext.merge(serviceContext);
        }
        return completeContext;
    }

}