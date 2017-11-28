package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.VolumeExtensionInterpreter;

@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(Volumes.class)
@ExtensionMarker(VolumeExtensionInterpreter.class)
public @interface Volume {

    boolean useClasspath() default false;
    
    String host();
    
    String container();
    
    AccessMode accessMode() default AccessMode.RW;
    
    public static enum AccessMode {
        RW, RO
    }
}
