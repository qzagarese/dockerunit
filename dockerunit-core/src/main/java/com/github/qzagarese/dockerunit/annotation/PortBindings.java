package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.PortBindingWrapperExtensionInterpreter;

@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(PortBindingWrapperExtensionInterpreter.class)
public @interface PortBindings {

    PortBinding[] value();
    
}
