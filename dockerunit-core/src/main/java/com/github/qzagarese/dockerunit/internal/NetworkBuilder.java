package com.github.qzagarese.dockerunit.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.qzagarese.dockerunit.Network;
import com.github.qzagarese.dockerunit.Service;

public interface NetworkBuilder {

    Network build(NetworkDescriptor descriptor, DockerClient client);

    Network cleanup(Network n, DockerClient client);

}
