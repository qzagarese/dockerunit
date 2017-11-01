package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Command;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;

public class CommandOptionBuilder implements OptionBuilder<Command>{

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, Command t) {
        return cmd.withCmd(t.value());
    }

}
