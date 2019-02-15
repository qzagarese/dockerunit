package com.github.qzagarese.dockerunit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Network {

    private final String name;
    private final String driver;
    private final String id;

}
