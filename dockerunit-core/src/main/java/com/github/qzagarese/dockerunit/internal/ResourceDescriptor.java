package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.annotation.Named;

import java.lang.reflect.Method;

public interface ResourceDescriptor {

    int getOrder();

    Named getNamed();

    Method getCustomisationHook();

    Object getInstance();

}
