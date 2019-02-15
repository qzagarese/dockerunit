package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.NetworkContext;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.NetworkContextBuider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DockerUnitSetup {

	private static final Logger logger = Logger.getLogger(DockerUnitSetup.class.getSimpleName());

	private final NetworkContextBuider networkContextBuider;
	private final ServiceContextBuilder serviceContextBuilder;
	private final DiscoveryProvider discoveryProvider;


    public NetworkContext setupNetworks(UsageDescriptor descriptor) {
        // TODO introduce network creation logic here.
        return null;
    }

	public ServiceContext setupServices(UsageDescriptor descriptor) {
		// Create containers and perform discovery one service at the time
		final AtomicBoolean failureOccured = new AtomicBoolean(false);
        List<ServiceContext> serviceContexts = descriptor.getDependencies().stream()
            .filter(rd -> rd instanceof ServiceDescriptor)
            .map(rd -> (ServiceDescriptor) rd)
            .map(serviceContextBuilder::buildServiceContext)
            .map(ctx -> {
                if (!ctx.checkStatus(Status.STARTED)) {
                	failureOccured.set(true);
                }
                return ctx;
            }).map(ctx -> {
                if(failureOccured.get()) {
                	logger.info("Skipping discovery of service " + getServiceName(ctx) + " due to a previous failure.");
                	return abortService(ctx);
                } 
            
                logger.info("Performing discovery for service " + getServiceName(ctx));
                ServiceContext postDiscoveryCtx = discoveryProvider.populateRegistry(ctx);
                if (!postDiscoveryCtx.checkStatus(Status.DISCOVERED)) {
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
			.map(si -> ensureStatus(si, Status.ABORTED, "Aborted due to previous failure."))
			.collect(Collectors.toSet());
	}

    private ServiceInstance ensureStatus(ServiceInstance si, Status status, String statusDetails) {
        return si.hasStatus(status) ? si : si.withStatus(status)
                .withStatusDetails(statusDetails);
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