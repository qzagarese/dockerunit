package com.github.qzagarese.dockerunit.internal.network;

import com.github.dockerjava.api.DockerClient;
import com.github.qzagarese.dockerunit.Network;
import com.github.qzagarese.dockerunit.internal.NetworkBuilder;
import com.github.qzagarese.dockerunit.internal.NetworkDescriptor;

public class DefaultNetworkBuilder implements NetworkBuilder {
    @Override
    public Network build(NetworkDescriptor descriptor, DockerClient client) {
        return null;
    }

    @Override
    public Network cleanup(Network n, DockerClient client) {
        return null;
    }
}
