package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.PortBindings;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class PortBindingWrapperExtensionInterpreter implements ExtensionInterpreter<PortBindings>{

    private PortBindingExtensionInterpreter builder = new PortBindingExtensionInterpreter();
    
    @Override
    public CreateContainerCmd build(ServiceDescriptor td, CreateContainerCmd cmd, PortBindings pbs) {
        for (PortBinding pb : pbs.value()) {
            cmd = builder.build(td, cmd, pb);
        }
        return cmd;
    }

}
