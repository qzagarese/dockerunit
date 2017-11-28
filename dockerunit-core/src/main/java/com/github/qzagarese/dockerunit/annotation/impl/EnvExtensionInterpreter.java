package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Env;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public class EnvExtensionInterpreter implements ExtensionInterpreter<Env>{

    @Override
    public CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, Env t) {
        return cmd.withEnv(t.value());
    }

}
