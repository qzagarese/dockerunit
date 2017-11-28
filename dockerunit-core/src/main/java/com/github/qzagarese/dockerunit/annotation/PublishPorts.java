package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.impl.PublishPortsExtensionInterpreter;

/**
 * 
 * Equivalent of {@value -P} in docker run
 * Tells Docker to map every port that is exposed by the container on a randomly
 * port on the host network interface.
 * 
 * Dockerunit automatically detects the ports and sets them in the {@linkplain ServiceInstance}
 * that is passed to the test.
 * 
 * @see ServiceContext
 * @see Service
 * @see ServiceInstance
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(PublishPortsExtensionInterpreter.class)
public @interface PublishPorts {

}
