package com.github.qzagarese.dockerunit.discovery.consul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.ExtensionMarker;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.impl.WebHealthCheckExtensionInterpreter;

/**
 * 
 * Allows the configuration of a health check endpoint so that Consul
 * can verify the state of each of the service replicas.
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtensionMarker(WebHealthCheckExtensionInterpreter.class)
public @interface WebHealthCheck {

	/**
	 * The path of the health check endpoint in your service.
	 * Default is {@value /health-check}
	 * 
	 * @return the health check endpoint
	 */
	String endpoint() default "/health-check";
	
	/**
	 * The web protocol. Default is HTTP.
	 * HTTP and HTTPS are supported 
	 * 
	 * @return the web protocol
	 */
	WebProtocol protocol() default WebProtocol.HTTP;
	
	/**
	 * The port that is exposed by the container (not the one it is mapped to on the host network interface)
	 * Default is 80
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
	
	public static enum WebProtocol {
		HTTP, HTTPS
	}
}
