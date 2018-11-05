package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.Volume;
import com.github.qzagarese.dockerunit.annotation.Volumes;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public class VolumeWrapperExtensionInterpreter implements ExtensionInterpreter<Volumes>{

    private VolumeExtensionInterpreter builder = new VolumeExtensionInterpreter();
    
    @Override
    public CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, Volumes vs) {
        for (Volume v : vs.value()) {
            cmd = builder.build(td, cmd, v);
        }
        return cmd;
    }

}
