package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.VolumeWrapperOptionBuilder;

@Retention(RUNTIME)
@Target(TYPE)
@OptionHandler(VolumeWrapperOptionBuilder.class)
public @interface Volumes {

    Volume[] value();
    
}
