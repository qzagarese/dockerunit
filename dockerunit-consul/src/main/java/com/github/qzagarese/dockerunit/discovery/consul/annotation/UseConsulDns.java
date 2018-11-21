package com.github.qzagarese.dockerunit.discovery.consul.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.ExtensionMarker;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.impl.UseConsulDnsExtensionInterpreter;

/**
 * Tells dockerunit to set consul as primary dns for the containers that are created from
 * the service descriptor class where this annotation is used.
 * 
 * If service A is defined using the {@link Named} annotation as follows
 * {@code @Named("service-a") }
 * 
 * then service B will be able to reference it using name <em> service-a.service.consul </em>
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(UseConsulDnsExtensionInterpreter.class)
public @interface UseConsulDns {

    
    
}
