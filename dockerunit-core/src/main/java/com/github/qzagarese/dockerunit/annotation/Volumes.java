package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.VolumeWrapperExtensionInterpreter;

/**
 * 
 * Wrapper annotation to allow repeated usage of {@linkplain Volume}
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(VolumeWrapperExtensionInterpreter.class)
public @interface Volumes {

    Volume[] value();
    
}
