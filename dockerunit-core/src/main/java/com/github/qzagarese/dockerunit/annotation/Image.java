package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Image {

    String value();
 
    PullStrategy pull() default PullStrategy.IF_ABSENT;
    
    public static enum PullStrategy {
    	ALWAYS, IF_ABSENT
    }
}
