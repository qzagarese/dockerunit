package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.NetworkContext;

public interface NetworkContextBuider {


    NetworkContext buildContext(UsageDescriptor descriptor);

    NetworkContext buildNetworkContext(NetworkDescriptor descriptor);

    NetworkContext clearContext(NetworkContext context);


}
