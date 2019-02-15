package com.github.qzagarese.dockerunit.annotation;


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
