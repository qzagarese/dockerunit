package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.ServiceContext;

public interface ServiceContextBuilder {

    ServiceContext buildContext(UsageDescriptor descriptor);

    ServiceContext buildServiceContext(ServiceDescriptor descriptor);
    
	ServiceContext clearContext(ServiceContext context); 
    
}
