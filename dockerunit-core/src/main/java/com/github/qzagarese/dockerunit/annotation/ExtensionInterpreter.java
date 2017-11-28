package com.github.qzagarese.dockerunit.annotation;

import java.lang.annotation.Annotation;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public interface ExtensionInterpreter<T extends Annotation> {

    CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, T t);
    
}
