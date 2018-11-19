package com.github.qzagarese.dockerunit.discovery.consul.annotation.impl;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_DEFAULT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_PROPERTY;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.UseConsulDns;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class UseConsulDnsExtensionInterpreter implements ExtensionInterpreter<UseConsulDns> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor td, CreateContainerCmd cmd, UseConsulDns t) {
        return cmd.withDns(System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT));
    }

}
