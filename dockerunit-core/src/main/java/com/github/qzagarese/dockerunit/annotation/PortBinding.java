package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.PortBindingExtensionInterpreter;

/**
 * 
 * Equivalent of {@value -p} option in docker run.
 * Exposes a container port on the host network interface.
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(PortBindings.class)
@ExtensionMarker(PortBindingExtensionInterpreter.class)
public @interface PortBinding {

	/**
	 * 
	 * @return the container port
	 */
    int exposedPort();
   
    /**
     * 
     * @return the host port where the container port is mapped
     */
    int hostPort();

    /**
     * 
     * @return the transport protocol. Default is TCP
     */
    Protocol protocol() default Protocol.TCP;
    
    /**
     * 
     * @return a specific host network interface ip.
     */
    String hostIp() default "";
    
    public static enum Protocol{
        TCP, UDP;
    }
    
}
