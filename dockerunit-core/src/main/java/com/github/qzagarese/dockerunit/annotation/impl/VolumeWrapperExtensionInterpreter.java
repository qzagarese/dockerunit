package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.Volume;
import com.github.qzagarese.dockerunit.annotation.Volumes;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class VolumeWrapperExtensionInterpreter implements ExtensionInterpreter<Volumes>{

    private VolumeExtensionInterpreter builder = new VolumeExtensionInterpreter();
    
    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, Volumes vs) {
        for (Volume v : vs.value()) {
            cmd = builder.build(sd, cmd, v);
        }
        return cmd;
    }

}
