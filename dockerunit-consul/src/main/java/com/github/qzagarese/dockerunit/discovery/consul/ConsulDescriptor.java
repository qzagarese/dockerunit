package com.github.qzagarese.dockerunit.discovery.consul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.qzagarese.dockerunit.annotation.ContainerBuilder;
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;

@Named("consul")
@Image("consul:1.0.0")
public class ConsulDescriptor {

	
	static final int CONSUL_DNS_PORT = 8600;
	static final int CONSUL_PORT = 8500;

	@ContainerBuilder
	public CreateContainerCmd setup(CreateContainerCmd cmd) {
		List<ExposedPort> ports = new ArrayList<>(Arrays.asList(cmd.getExposedPorts()));
        ExposedPort dnsPort = ExposedPort.udp(CONSUL_DNS_PORT);
        ports.add(dnsPort);
        
        ExposedPort consulPort = ExposedPort.tcp(CONSUL_PORT);
        ports.add(consulPort);
        
        Ports bindings = cmd.getPortBindings();
        if (bindings == null) {
            bindings = new Ports();
        }
        
        bindings.bind(dnsPort, Binding.bindPort(8600));
        bindings.bind(consulPort, Binding.bindPort(8500));

        return cmd.withExposedPorts(ports)
            .withPortBindings(bindings)
            .withCmd("agent", "-dev", "-client=0.0.0.0", "-enable-script-checks");

	}
	
}
