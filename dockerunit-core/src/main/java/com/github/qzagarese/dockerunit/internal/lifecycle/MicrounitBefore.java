package com.github.qzagarese.dockerunit.internal.lifecycle;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.MicrounitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MicrounitBefore extends Statement {

	private final FrameworkMethod method;
	private final MicrounitRunner runner;
	private final Statement next;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	private final DependencyDescriptor descriptor;
	
	@Override
	public void evaluate() throws Throwable {
        ServiceContext context = contextBuilder.buildContext(descriptor);
        runner.setContext(method, context);
        if(!context.allHealthy()) {
        	throw new RuntimeException(context.getFormattedErrors());
        }
        context = discoveryProvider.populateRegistry(context.mergeInstances(runner.getClassContext()));
        runner.setContext(method, context);
        if(!context.allHealthy()) {
        	throw new RuntimeException(context.getFormattedErrors());
        }
        next.evaluate();
	}

}
