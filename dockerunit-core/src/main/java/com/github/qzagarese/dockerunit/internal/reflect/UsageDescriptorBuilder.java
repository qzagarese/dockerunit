package com.github.qzagarese.dockerunit.internal.reflect;

import org.junit.runners.model.FrameworkMethod;

import com.github.qzagarese.dockerunit.internal.UsageDescriptor;

public interface UsageDescriptorBuilder {

    UsageDescriptor buildDescriptor(FrameworkMethod method);
    
    UsageDescriptor buildDescriptor(Class<?> klass);
    
}
