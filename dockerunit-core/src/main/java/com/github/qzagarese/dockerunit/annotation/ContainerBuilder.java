package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.dockerjava.api.command.CreateContainerCmd;

/**
 * 
 * Marker annotation allowing container customisations.
 * By providing a method that accepts an argument of type {@linkplain CreateContainerCmd} 
 * and returning a {@linkplain CreateContainerCmd}, you can use this annotation
 * to apply any settings that are allowed by docker-java.
 * 
 * The provided method will be executed after all the {@linkplain ExtensionMarker} 
 * interpreters have been executed. 
 * 
 * The example below shows how to set the Google dns as the dns for your container
 * <pre>
 * &#64;ContainerBuilder
 * public CreateContainerCmd build(CreateContainerCmd cmd) {
 * 	return cmd.withDns("8.8.4.4");
 * }
 * </pre>
 *
 *@see ExtensionMarker
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ContainerBuilder {

}
