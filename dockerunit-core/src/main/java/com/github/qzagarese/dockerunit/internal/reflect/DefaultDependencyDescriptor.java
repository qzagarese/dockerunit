package com.github.qzagarese.dockerunit.internal.reflect;

import com.github.qzagarese.dockerunit.internal.ResourceDescriptor;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DefaultDependencyDescriptor implements UsageDescriptor {

	private List<ResourceDescriptor> dependencies;
	
	public List<ResourceDescriptor> getDependencies() {
		dependencies.sort((d1, d2) -> d1.getOrder() - d2.getOrder());
		return dependencies;
	}

}
