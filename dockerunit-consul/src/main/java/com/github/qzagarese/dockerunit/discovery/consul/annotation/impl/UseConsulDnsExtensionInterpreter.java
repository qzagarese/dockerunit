package com.github.qzagarese.dockerunit.discovery.consul.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.discovery.consul.ContainerUtils;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.UseConsulDns;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;
import com.github.qzagarese.dockerunit.internal.docker.DefaultDockerClientProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_DEFAULT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_PROPERTY;

public class UseConsulDnsExtensionInterpreter implements ExtensionInterpreter<UseConsulDns> {

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, UseConsulDns t) {
        Optional<String> dnsIp = Optional.ofNullable(ContainerUtils.extractBridgeIpAddress(ContainerUtils.getConsulContainer()
                .getNetworkSettings()).get());

        List<String> dnsList = Arrays.asList(Optional.ofNullable(cmd.getDns()).orElse(new String[0]));
        dnsList.add(dnsIp.orElse(System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT)));
        return cmd.withDns(dnsList);
    }



}
