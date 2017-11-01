package com.github.qzagarese.dockerunit.internal.lifecycle;

import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DockerUnitAfterClass extends Statement {

	private final DockerUnitRunner runner;
	private final Statement statement;
	private final DiscoveryProvider discoveryProvider;
	private final ServiceContextBuilder contextBuilder;
	
	@Override
	public void evaluate() throws Throwable {
		try {
			statement.evaluate();
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			ServiceContext context = runner.getClassContext();
			if(context != null) {
				ServiceContext cleared = contextBuilder.clearContext(context);
				discoveryProvider.clearRegistry(cleared, cleared);
			}
			ServiceContext discoveryContext = runner.getDiscoveryContext();
			if(discoveryContext != null) {	
				contextBuilder.clearContext(discoveryContext);
			}
		}
	}

}
