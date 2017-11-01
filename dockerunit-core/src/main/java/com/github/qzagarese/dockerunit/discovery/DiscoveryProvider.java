package com.github.qzagarese.dockerunit.discovery;

import com.github.qzagarese.dockerunit.ServiceContext;

public interface DiscoveryProvider {

	Class<?> getDiscoveryConfig();

	ServiceContext populateRegistry(ServiceContext context);
	
	ServiceContext clearRegistry(ServiceContext context, ServiceContext globalContext);

}
