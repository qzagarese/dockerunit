package com.github.qzagarese.dockerunit;

import java.util.Set;

public interface ServiceContext {

    
    Set<Service> getServices();
    
    Service getService(String name);
    
    ServiceContext merge(ServiceContext context);
    
	ServiceContext mergeInstances(ServiceContext classContext);
	
	ServiceContext subtract(ServiceContext context);
	
	boolean allHealthy();
	
	String getFormattedErrors();
    
}
