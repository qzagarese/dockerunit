package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.qzagarese.dockerunit.annotation.ContainerBuilder;
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.UseConsulDns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.github.qzagarese.dockerunit.discovery.consul.ConsulDiscoveryConfig.*;

@Named("consul")
@Image("consul:1.0.0")
public class ConsulDescriptor {

	
	static final int CONSUL_DNS_PORT = 8600;
	static final int CONSUL_PORT = 8500;

    private static final Logger logger = Logger.getLogger(ConsulDescriptor.class.getSimpleName());


    @ContainerBuilder
	public CreateContainerCmd setup(CreateContainerCmd cmd) {
		List<ExposedPort> ports = new ArrayList<>(Arrays.asList(cmd.getExposedPorts()));

        ExposedPort consulPort = ExposedPort.tcp(CONSUL_PORT);
        ports.add(consulPort);
        
        Ports bindings = cmd.getPortBindings();
        if (bindings == null) {
            bindings = new Ports();
        }
        
        Optional<String> disableDnsFlag = Optional.ofNullable(System.getProperty(CONSUL_DNS_OFF_PROPERTY, CONSUL_DNS_OFF_DEFAULT));

        if(!disableDnsFlag.isPresent()) {
            activateDns(ports, bindings);
        } else {
            logger.warning("Consul dns has been disabled. Usages of @" + UseConsulDns.class.getSimpleName() + " will not sort any effect.");
        }
        
        bindings.bind(consulPort, Binding.bindPort(8500));

        return cmd.withExposedPorts(ports)
            .withPortBindings(bindings)
            .withCmd("agent", "-dev", "-client=0.0.0.0", "-enable-script-checks");

	}

    private void activateDns(List<ExposedPort> ports, Ports bindings) {
        ExposedPort dnsPort = ExposedPort.udp(CONSUL_DNS_PORT);
        ports.add(dnsPort);
        
        int dnsBridgePort = Integer.parseInt(System.getProperty(CONSUL_DNS_PORT_BRIDGE_BINDING,
                CONSUL_DNS_PORT_BRIDGE_BINDING_DEFAULT));

        bindings.bind(dnsPort, Binding.bindIpAndPort(
                System.getProperty(DOCKER_BRIDGE_IP_PROPERTY, DOCKER_BRIDGE_IP_DEFAULT),
                dnsBridgePort));
    }

}
