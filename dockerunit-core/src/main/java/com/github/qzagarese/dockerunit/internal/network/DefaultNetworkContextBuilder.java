package com.github.qzagarese.dockerunit.internal.network;

import com.github.qzagarese.dockerunit.NetworkContext;
import com.github.qzagarese.dockerunit.internal.NetworkContextBuider;
import com.github.qzagarese.dockerunit.internal.NetworkDescriptor;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;

public class DefaultNetworkContextBuilder implements NetworkContextBuider {

    @Override
    public NetworkContext buildContext(UsageDescriptor descriptor) {
        return null;
    }

    @Override
    public NetworkContext clearContext(NetworkContext context) {
        return null;
    }
}
