package com.github.qzagarese.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.Env;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class EnvExtensionInterpreter implements ExtensionInterpreter<Env> {

	@Override
	public CreateContainerCmd build(ServiceDescriptor td, CreateContainerCmd cmd, Env t) {
		String[] env = cmd.getEnv();
		List<String> finalEnv = new ArrayList<>();
		finalEnv.addAll(Arrays.asList(t.value()));
		if (env != null) {
			finalEnv.addAll(Arrays.asList(env));
		}
		return cmd.withEnv(finalEnv);
	}

}
