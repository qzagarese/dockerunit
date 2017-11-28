package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Meta-annotation that allows the creation of extensions for Dockerunit.
 * 
 * 1) Create your annotation.}
 * 2) Mark it with {@code @ExtensionMarker}
 * 3) Create an interpreter that will apply changes to the 
 * 	  Docker container based on the annotation value.
 * 
 * @see ExtensionInterpreter
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface ExtensionMarker {
	
	/**
	 * The class that will interpret instances of your annotation
	 * 
	 * @return the interpreter type
	 */
    Class<? extends ExtensionInterpreter<?>> value();
    
}
