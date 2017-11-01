package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.internal.service.DefaultServiceBuilder;

public class ServiceBuilderFactory {

    private static final DefaultServiceBuilder INSTANCE = new DefaultServiceBuilder();

    public static ServiceBuilder create() {
        return INSTANCE;
    }
    
}
