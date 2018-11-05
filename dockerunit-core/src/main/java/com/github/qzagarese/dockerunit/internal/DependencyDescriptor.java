package com.github.qzagarese.dockerunit.internal;

import java.util.List;

public interface DependencyDescriptor {

    List<TestDescriptor> getDependencies();
    
}
