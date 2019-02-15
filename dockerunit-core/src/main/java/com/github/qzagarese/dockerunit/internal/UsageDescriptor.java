package com.github.qzagarese.dockerunit.internal;

import java.util.List;

public interface UsageDescriptor {

    List<ResourceDescriptor> getDependencies();
    
}
