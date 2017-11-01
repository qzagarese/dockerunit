package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;
import com.github.qzagarese.dockerunit.annotation.Volume;
import com.github.qzagarese.dockerunit.annotation.Volumes;

public class VolumeWrapperOptionBuilder implements OptionBuilder<Volumes>{

    private VolumeOptionBuilder builder = new VolumeOptionBuilder();
    
    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, Volumes vs) {
        for (Volume v : vs.value()) {
            cmd = builder.build(cmd, v);
        }
        return cmd;
    }

}
