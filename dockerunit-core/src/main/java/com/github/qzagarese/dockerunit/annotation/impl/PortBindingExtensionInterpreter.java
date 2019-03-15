package com.github.qzagarese.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.PortBinding.Protocol;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class PortBindingExtensionInterpreter implements ExtensionInterpreter<PortBinding> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, PortBinding pb) {
        ExposedPort containerPort = toExposedPort(pb);

        List<ExposedPort> ports = new ArrayList<>(Arrays.asList(cmd.getExposedPorts()));
        ports.add(containerPort);

        HostConfig hc = cmd.getHostConfig();

        Ports bindings = Optional.ofNullable(hc.getPortBindings()).orElse(new Ports());
        bindings.bind(containerPort, toHostBinding(pb));

        return cmd.withExposedPorts(ports).withHostConfig(hc.withPortBindings(bindings));
    }

    private ExposedPort toExposedPort(PortBinding pb) {
        return pb.protocol() == Protocol.TCP ? ExposedPort.tcp(pb.exposedPort()) : ExposedPort.udp(pb.exposedPort());
    }

    private Binding toHostBinding(PortBinding pb) {
        return pb.hostIp().isEmpty()
               ? Binding.bindPort(pb.hostPort())
               : Binding.bindIpAndPort(pb.hostIp(), pb.hostPort());
    }
}
