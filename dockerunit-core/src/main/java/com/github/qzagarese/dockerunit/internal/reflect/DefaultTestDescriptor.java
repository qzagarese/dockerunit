package com.github.qzagarese.dockerunit.internal.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DefaultTestDescriptor implements TestDescriptor {

    private Image image;
    private Named named;
    private List<? extends Annotation> options;
    private Method customisationHook;
    private int replicas;
    private int order;
    private String containerName;
    private Object instance;
    
}
