package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Repeatable(Dependencies.class)
public @interface Use {

    Class<?> service();
    String containerPrefix() default "";
    int replicas() default 1;
    int order() default 0;
    
}
