package com.github.qzagarese.dockerunit.internal.reflect;

import java.util.List;

import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.TestDependency;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultDependencyDescriptor implements DependencyDescriptor {

	private List<TestDependency> dependencies;
	
	public List<TestDependency> getDependencies() {
		dependencies.sort((d1, d2) -> d1.getOrder() - d2.getOrder());
		return dependencies;
	}

}
