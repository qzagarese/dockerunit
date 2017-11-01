package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.PortBindingOptionBuilder;

@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(PortBindings.class)
@OptionHandler(PortBindingOptionBuilder.class)
public @interface PortBinding {

    int exposedPort();
   
    int hostPort();

    Protocol protocol() default Protocol.TCP;
    
    String hostIp() default "";
    
    public static enum Protocol{
        TCP, UDP;
    }
    
}
