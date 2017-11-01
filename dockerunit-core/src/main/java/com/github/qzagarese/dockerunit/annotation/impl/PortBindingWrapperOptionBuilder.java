package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.PortBindings;

public class PortBindingWrapperOptionBuilder implements OptionBuilder<PortBindings>{

    private PortBindingOptionBuilder builder = new PortBindingOptionBuilder();
    
    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, PortBindings pbs) {
        for (PortBinding pb : pbs.value()) {
            cmd = builder.build(cmd, pb);
        }
        return cmd;
    }

}
