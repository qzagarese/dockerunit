package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Command;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;

public class CommandExtensionInterpreter implements ExtensionInterpreter<Command>{

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, Command t) {
        return cmd.withCmd(t.value());
    }

}
