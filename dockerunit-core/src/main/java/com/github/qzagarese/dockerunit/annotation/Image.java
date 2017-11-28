package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Sets the image that must be used to instantiate containers for this service.
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Image {

	/**
	 * The name of the image. It can contain a registry. 
	 * If so, Dockerunit will automatically pull the image 
	 * according to the selected {@linkplain PullStrategy}. 
	 * @return the name of the image
	 */
    String value();
 
    /**
     * Instructs Dockerunit about when to pull the specified image.
     * By default, Dockerunit pulls the image only if none has been found locally.
     * @return the {@linkplain PullStrategy}
     */
    PullStrategy pull() default PullStrategy.IF_ABSENT;
    
    public static enum PullStrategy {
    	/**
    	 * pull the image always, even when one is found locally.
    	 */
    	ALWAYS,
    	/**
    	 * pull the image only if it is not found locally.
    	 */
    	IF_ABSENT
    }
}
