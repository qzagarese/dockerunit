package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.qzagarese.dockerunit.internal.docker.DefaultDockerClientProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ContainerUtils {

    private static final com.github.dockerjava.api.DockerClient dockerClient = new DefaultDockerClientProvider().getClient();


    public static Optional<String> extractBridgeIpAddress(ContainerNetworkSettings settings) {
        return extractIp(settings.getNetworks());
    }

    public static Optional<String> extractBridgeIpAddress(NetworkSettings settings) {
        return extractIp(settings.getNetworks());
    }

    public static Optional<Integer> extractMappedPort(int port, NetworkSettings networkSettings) {
        return networkSettings.getPorts().getBindings().entrySet().stream()
                .filter(entry -> entry.getKey().getPort() == port)
                .map(Map.Entry::getValue)
                .filter(bindings -> bindings != null && bindings.length > 0)
                .findFirst()
                .map(bindings ->  parsePort(bindings[0].getHostPortSpec())
                        .orElseThrow(() -> new RuntimeException(String.format("Could not parse mapping for exposed port ", port))));
    }

    public static Container getConsulContainer() {
        return dockerClient.listContainersCmd().exec().stream()
                .filter(c -> isConsul(c))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Could not detect the Consul container.")));
    }


    private static boolean isConsul(Container c) {
        return Arrays.stream(c.getNames()).anyMatch(s -> s.equals(ConsulDiscoveryConfig.CONSUL_CONTAINER_NAME));
    }


    private static Optional<String> extractIp(Map<String, ContainerNetwork> networks) {
        return Optional.ofNullable(networks.entrySet().stream()
                .filter(network -> "bridge".equals(network.getKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(("Container is not connected to the bridge network.")))
                .getValue()
                .getIpAddress());
    }

    private static Optional<Integer> parsePort(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }
}
