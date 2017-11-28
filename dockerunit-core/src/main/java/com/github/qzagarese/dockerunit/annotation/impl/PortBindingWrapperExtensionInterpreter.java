package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.PortBindings;

public class PortBindingWrapperExtensionInterpreter implements ExtensionInterpreter<PortBindings>{

    private PortBindingExtensionInterpreter builder = new PortBindingExtensionInterpreter();
    
    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, PortBindings pbs) {
        for (PortBinding pb : pbs.value()) {
            cmd = builder.build(cmd, pb);
        }
        return cmd;
    }

}
