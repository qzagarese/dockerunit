package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.PortBindingWrapperOptionBuilder;

@Retention(RUNTIME)
@Target(TYPE)
@OptionHandler(PortBindingWrapperOptionBuilder.class)
public @interface PortBindings {

    PortBinding[] value();
    
}
