package com.github.qzagarese.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.Volume;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class VolumeExtensionInterpreter implements ExtensionInterpreter<Volume> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, Volume v) {
        List<Bind> binds = Optional.ofNullable(cmd.getHostConfig().getBinds())
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());

        binds.add(
                new Bind(
                        toHostPath(v),
                        new com.github.dockerjava.api.model.Volume(v.container()),
                        AccessMode.fromBoolean(v.accessMode() == Volume.AccessMode.RW)
                )
        );

        HostConfig hc = cmd.getHostConfig().withBinds(binds);

        return cmd.withHostConfig(hc);
    }

    private String toHostPath(Volume v) {
        return v.useClasspath()
               ? Thread.currentThread().getContextClassLoader().getResource(v.host()).getPath()
               : v.host();
    }
}
