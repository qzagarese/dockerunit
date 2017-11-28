package com.github.qzagarese.dockerunit.discovery.consul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.ExtensionMarker;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.impl.WebHealthCheckExtensionInterpreter;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtensionMarker(WebHealthCheckExtensionInterpreter.class)
public @interface WebHealthCheck {

	String endpoint() default "/health-check";
	WebProtocol protocol() default WebProtocol.HTTP;
	int exposedPort() default 80;
	int pollingInterval() default 1;
	
	public static enum WebProtocol {
		HTTP, HTTPS
	}
}
