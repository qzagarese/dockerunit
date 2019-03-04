package com.github.qzagarese.dockerunit.discovery.consul.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.UseConsulDns;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;
import com.github.qzagarese.dockerunit.internal.docker.DefaultDockerClientProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_DEFAULT;
import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.DOCKER_BRIDGE_IP_PROPERTY;

public class UseConsulDnsExtensionInterpreter implements ExtensionInterpreter<UseConsulDns> {


    private final com.github.dockerjava.api.DockerClient dockerClient;

    public UseConsulDnsExtensionInterpreter() {
        dockerClient = new DefaultDockerClientProvider().getClient();
    }

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, UseConsulDns t) {
        ListContainersCmd listCmd = dockerClient.listContainersCmd();
        Map<String, List<String>> filters = listCmd.getFilters();
        filters.put("name", Arrays.asList(ConsulDiscoveryConfig.CONSUL_CONTAINER_NAME));

        Container container = listCmd.exec().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Could not detect the Consul container.")));

        Map.Entry<String, ContainerNetwork> bridge = container.getNetworkSettings().getNetworks().entrySet().stream()
                .filter(entry -> entry.getKey().equals("bridge"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Container is not connected to the bridge network.")));

        String ipAddress = bridge.getValue().getIpAddress();
        return cmd.withDns(ipAddress != null ? ipAddress : System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT));
    }

}
