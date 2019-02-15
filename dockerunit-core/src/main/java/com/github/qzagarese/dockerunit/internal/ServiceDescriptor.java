package com.github.qzagarese.dockerunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;

public interface ServiceDescriptor extends  ResourceDescriptor {

    Image getImage();
    
    List<? extends Annotation> getOptions();

    String getContainerName();
    
    int getReplicas();

}
