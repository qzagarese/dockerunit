package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Env;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;

public class EnvOptionBuilder implements OptionBuilder<Env>{

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, Env t) {
        return cmd.withEnv(t.value());
    }

}
