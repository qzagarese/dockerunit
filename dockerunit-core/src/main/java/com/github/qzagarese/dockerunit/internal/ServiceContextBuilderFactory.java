package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContextBuilder;

public class ServiceContextBuilderFactory {

    private static final DefaultServiceContextBuilder INSTANCE = new DefaultServiceContextBuilder();

    public static ServiceContextBuilder create() {
        return INSTANCE;
    }
    
}
