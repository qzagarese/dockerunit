package com.github.qzagarese.dockerunit.discovery.consul.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.EnableConsul;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public class EnableConsulExtensionInterpreter implements ExtensionInterpreter<EnableConsul> {

	private static final String SERVICE_NAME_SUFFIX = "_NAME";
	private static final String SERVICE_PREFIX = "SERVICE_";
	private static final String SERVICE_CHECK_INTERVAL = SERVICE_PREFIX + "CHECK_INTERVAL";
	
	@Override
	public CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, EnableConsul ec) {
		final String serviceNameEnv = SERVICE_PREFIX + ec.exposedPort() + SERVICE_NAME_SUFFIX + "=" + td.getNamed().value();
		final String serviceCheckIntervalEnv = SERVICE_CHECK_INTERVAL + "=" + ec.pollingInterval() + "s";
		
		List<String> finalEnv = new ArrayList<>();
		List<String> enableConsulEnv = Arrays.asList(serviceNameEnv, serviceCheckIntervalEnv);
		finalEnv.addAll(enableConsulEnv);

		String[] env = cmd.getEnv();
		
		if(env != null) {
			finalEnv.addAll(Arrays.asList(env));
		}
		return cmd.withEnv(finalEnv);
	}

}
