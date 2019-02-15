package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.NetworkDefinition;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class NetworkDefinitionInterpreter implements ExtensionInterpreter<NetworkDefinition> {
    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, NetworkDefinition networkDefinition) {
        return null;
    }
}
