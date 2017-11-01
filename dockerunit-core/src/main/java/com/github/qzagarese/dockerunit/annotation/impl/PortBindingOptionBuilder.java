package com.github.qzagarese.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.PortBinding.Protocol;

public class PortBindingOptionBuilder implements OptionBuilder<PortBinding> {

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, PortBinding pb) {
        List<ExposedPort> ports = new ArrayList<>(Arrays.asList(cmd.getExposedPorts()));
        ExposedPort containerPort = pb.protocol()
            .equals(Protocol.TCP) ? ExposedPort.tcp(pb.exposedPort()) : ExposedPort.udp(pb.exposedPort());
        ports.add(containerPort);
        
        Ports bindings = cmd.getPortBindings();
        if (bindings == null) {
            bindings = new Ports();
        }
        bindings.bind(containerPort, pb.hostIp()
            .isEmpty() ? Binding.bindPort(pb.hostPort()) : Binding.bindIpAndPort(pb.hostIp(), pb.hostPort()));

        return cmd.withExposedPorts(ports)
            .withPortBindings(bindings);
    }

}
