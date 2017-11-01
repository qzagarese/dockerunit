package com.github.qzagarese.dockerunit.annotation.impl;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.OptionBuilder;
import com.github.qzagarese.dockerunit.annotation.PublishPorts;

public class PublishPortsOptionBuilder implements OptionBuilder<PublishPorts> {

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, PublishPorts t) {
        return cmd.withPublishAllPorts(true);
    }

}
