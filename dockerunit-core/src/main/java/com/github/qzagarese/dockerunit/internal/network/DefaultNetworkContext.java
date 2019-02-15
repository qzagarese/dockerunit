package com.github.qzagarese.dockerunit.internal.network;

import com.github.qzagarese.dockerunit.Network;
import com.github.qzagarese.dockerunit.NetworkContext;

import java.util.Set;

public class DefaultNetworkContext implements NetworkContext {

    @Override
    public Set<Network> getNetworks() {
        return null;
    }

    @Override
    public Network getNetwork(String name) {
        return null;
    }
}
