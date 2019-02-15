package com.github.qzagarese.dockerunit;

import java.util.Set;

public interface NetworkContext {


    Set<Network> getNetworks();

    Network getNetwork(String name);

}
