package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.internal.network.DefaultNetworkContextBuilder;

public class NetworkContextBuilderFactory {

    private static final DefaultNetworkContextBuilder INSTANCE = new DefaultNetworkContextBuilder();

    public static NetworkContextBuider create() {
        return INSTANCE;
    }

}
