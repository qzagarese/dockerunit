package com.github.qzagarese.dockerunit.internal.reflect;

import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.annotation.NetworkDefinition;
import com.github.qzagarese.dockerunit.internal.NetworkDescriptor;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;


@Getter
@Builder
public class DefaultNetworkDescriptor implements NetworkDescriptor {

    private Named named;
    private NetworkDefinition networkDefinition;
    private Method customisationHook;
    private int order;
    private Object instance;

}
