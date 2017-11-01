package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.ServiceContext;

public interface ServiceContextBuilder {

    ServiceContext buildContext(DependencyDescriptor descriptor);

	ServiceContext clearContext(ServiceContext context); 
    
}
