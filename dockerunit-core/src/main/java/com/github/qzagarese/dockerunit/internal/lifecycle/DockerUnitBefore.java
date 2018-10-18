package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitBefore extends Statement {

	private final FrameworkMethod method;
	private final DockerUnitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	private final UsageDescriptor descriptor;
	
	@Override
	public void evaluate() throws Throwable {
	    
	    List<ServiceContext> serviceContexts = new ArrayList<>();
	    
	    descriptor.getDependencies().forEach(svc -> {
	        // Build containers
	        ServiceContext singleSvcContext = contextBuilder.buildServiceContext(svc);
	        if(!singleSvcContext.allHealthy()) {
	            throw new RuntimeException(singleSvcContext.getFormattedErrors());
	        }
	        
	        // Merge with class context in case there are more instances of this service at class level
	        singleSvcContext = singleSvcContext.merge(findEquivalentInClass(svc.getNamed().value(), runner.getClassContext()));
	        
	        // Perform discovery
	        singleSvcContext = discoveryProvider.populateRegistry(singleSvcContext);
	        if(!singleSvcContext.allHealthy()) {
                throw new RuntimeException(singleSvcContext.getFormattedErrors());
            }
	        serviceContexts.add(singleSvcContext);
	    });
	    
	    ServiceContext methodLevelContext = mergeContexts(serviceContexts);
	    if(methodLevelContext == null) {
	        methodLevelContext = new DefaultServiceContext(new HashSet<>());
	    }
	    methodLevelContext = methodLevelContext.merge(runner.getClassContext());
    
	    runner.setContext(method, methodLevelContext);
        if(!methodLevelContext.allHealthy()) {
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
