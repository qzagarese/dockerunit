package com.github.qzagarese.dockerunit.internal.reflect;

import org.junit.runners.model.FrameworkMethod;

import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;

public interface DependencyDescriptorBuilder {

    DependencyDescriptor buildDescriptor(FrameworkMethod method);
    
    DependencyDescriptor buildDescriptor(Class<?> klass);
    
}
