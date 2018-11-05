package com.github.qzagarese.dockerunit.internal.lifecycle;

import java.util.HashSet;

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
	private final ServiceContextBuilder contextBuilder;
	private final UsageDescriptor descriptor;
	
	@Override
	public void evaluate() throws Throwable {
	    
	    ServiceContext methodLevelContext = new DockerUnitSetup(contextBuilder, discoveryProvider).setup(descriptor);
		
	    if (methodLevelContext == null) {
	        methodLevelContext = new DefaultServiceContext(new HashSet<>());
	    }
	    methodLevelContext = methodLevelContext.merge(runner.getClassContext());
    
	    runner.setContext(method, methodLevelContext);
        if (!methodLevelContext.checkStatus(Status.DISCOVERED)) {
        	throw new RuntimeException(methodLevelContext.getFormattedErrors());
        }
        next.evaluate();
	}
	
}
