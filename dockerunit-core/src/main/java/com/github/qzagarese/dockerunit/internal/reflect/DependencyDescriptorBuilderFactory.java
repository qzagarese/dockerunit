package com.github.qzagarese.dockerunit.internal.reflect;


public class DependencyDescriptorBuilderFactory {

    
    
    private static final DefaultDependencyDescriptorBuilder INSTANCE = new DefaultDependencyDescriptorBuilder();

    public static  DependencyDescriptorBuilder create(){
        return INSTANCE;
    }
    
}
