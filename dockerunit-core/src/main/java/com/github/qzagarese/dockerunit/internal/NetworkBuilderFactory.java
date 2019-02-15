package com.github.qzagarese.dockerunit.internal;

import com.github.qzagarese.dockerunit.internal.network.DefaultNetworkBuilder;

public class NetworkBuilderFactory {

    private static final DefaultNetworkBuilder INSTANCE = new DefaultNetworkBuilder();

    public static NetworkBuilder create() {
        return INSTANCE;
    }

}
