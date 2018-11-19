package com.github.qzagarese.dockerunit.discovery.consul.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.ExtensionMarker;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.impl.UseConsulDnsExtensionInterpreter;

@Retention(RUNTIME)
@Target(TYPE)
@ExtensionMarker(UseConsulDnsExtensionInterpreter.class)
public @interface UseConsulDns {

    
    
}
