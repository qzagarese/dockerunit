package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBefore extends Statement {

    private static Logger logger = Logger.getLogger(DockerUnitBefore.class.getName());
    
	private final FrameworkMethod method;
	private final DockerUnitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	private final UsageDescriptor descriptor;
	
	@Override
	public void evaluate() throws Throwable {
	    
	    // Create containers and perform discovery one service at the time
        List<ServiceContext> serviceContexts = descriptor.getDependencies().stream()
            .map(contextBuilder::buildServiceContext)
            .map(ctx -> {
                if (!ctx.checkStatus(Status.STARTED)) {
                    throw new RuntimeException(ctx.getFormattedErrors());
                }
                logger.info("Performing discovery for service " + ctx.getServices().stream().findFirst().get().getName());
                return discoveryProvider.populateRegistry(ctx);
            })
            .collect(Collectors.toList());  
	    
	    ServiceContext methodLevelContext = mergeContexts(serviceContexts);
	    if(methodLevelContext == null) {
	        methodLevelContext = new DefaultServiceContext(new HashSet<>());
	    }
	    methodLevelContext = methodLevelContext.merge(runner.getClassContext());
    
	    runner.setContext(method, methodLevelContext);
        if(!methodLevelContext.checkStatus(Status.DISCOVERED)) {
        	throw new RuntimeException(methodLevelContext.getFormattedErrors());
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
	
    private ServiceContext findEquivalentInClass(String svcName, ServiceContext classContext) {
        Set<Service> equivalentContainer = new HashSet<>();
        Service svc = classContext.getService(svcName);
        if (svc != null) {
            equivalentContainer.add(svc);
        }
        return new DefaultServiceContext(equivalentContainer);
    }

}
