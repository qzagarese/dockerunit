package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Command;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class CommandExtensionInterpreter implements ExtensionInterpreter<Command>{

    @Override
    public CreateContainerCmd build(ServiceDescriptor sd, CreateContainerCmd cmd, Command c) {
        return cmd.withCmd(c.value());
    }

}
