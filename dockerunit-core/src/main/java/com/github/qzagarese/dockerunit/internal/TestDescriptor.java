package com.github.qzagarese.dockerunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;

public interface TestDescriptor {

    Image getImage();
    
    Named getNamed();
    
    List<? extends Annotation> getOptions();
    
    Method getCustomisationHook();
    
    Object getInstance();
    
    String getContainerName();
    
    int getReplicas();
    
    int getOrder();
    
}
