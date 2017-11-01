package com.github.qzagarese.dockerunit.annotation;

import java.lang.annotation.Annotation;

import com.github.dockerjava.api.command.CreateContainerCmd;

public interface OptionBuilder<T extends Annotation> {

    CreateContainerCmd build(CreateContainerCmd cmd, T t);
    
}
