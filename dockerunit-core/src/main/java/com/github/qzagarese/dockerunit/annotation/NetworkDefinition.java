package com.github.qzagarese.dockerunit.annotation;


import com.github.qzagarese.dockerunit.annotation.impl.NetworkDefinitionInterpreter;

@ExtensionMarker(NetworkDefinitionInterpreter.class)
public @interface NetworkDefinition {

    /**
     *
     * @return the name of the network
     */
    String name();

    /**
     *
     * @return the network driver mode. Defaults to 'bridge'
     */
    String driver() default "bridge";


}
