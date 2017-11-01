package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.CommandOptionBuilder;


@Retention(RUNTIME)
@Target(TYPE)
@OptionHandler(CommandOptionBuilder.class)
public @interface Command {

    String[] value();
    
}
