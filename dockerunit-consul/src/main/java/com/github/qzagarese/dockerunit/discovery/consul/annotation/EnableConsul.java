package com.github.qzagarese.dockerunit.discovery.consul.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.ExtensionMarker;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.impl.EnableConsulExtensionInterpreter;

/**
 * 
 * Allows Consul to probe each replica of your service. This enables basic
 * discovery by telling Consul about your service by means of registrator. If
 * you are creating an HTTP/HTTPS service, you should expose a health-check
 * endpoint and use {@linkplain WebHealthCheck}
 * 
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(EnableConsulExtensionInterpreter.class)
public @interface EnableConsul {

	/**
	 * The port that is exposed by the container (not the one it is mapped to on the
	 * host network interface) Default is 80
	 * 
	 * @return the port number
	 */
	int exposedPort() default 80;
	
	/**
	 * The length of the interval (in seconds) Consul will wait before re-checking the service state.
	 * Default is 1 second.
	 * 
	 * @return the interval in seconds
	 */
	int pollingInterval() default 1;

}
