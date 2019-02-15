package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.annotation.NetworkDefinition;

public interface NetworkDescriptor extends ResourceDescriptor {

    NetworkDefinition getNetworkDefinition();

}
