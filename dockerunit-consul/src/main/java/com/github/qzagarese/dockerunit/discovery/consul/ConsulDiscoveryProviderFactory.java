package com.github.qzagarese.dockerunit.discovery.consul;

import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;

public class ConsulDiscoveryProviderFactory implements DiscoveryProviderFactory {

	@Override
	public DiscoveryProvider getProvider() {
		return new ConsulDiscoveryProvider();
	}

}
