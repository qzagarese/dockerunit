package com.github.qzagarese.dockerunit.discovery.consul.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck.WebProtocol;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

public class WebHealthCheckExtensionInterpreter implements ExtensionInterpreter<WebHealthCheck> {

	private static final String SERVICE_NAME_SUFFIX = "_NAME";
	private static final String SERVICE_PREFIX = "SERVICE_";
	private static final String SERVICE_CHECK_SCRIPT = SERVICE_PREFIX + "CHECK_SCRIPT";
	private static final String SERVICE_CHECK_INTERVAL = SERVICE_PREFIX + "CHECK_INTERVAL";

	@Override
	public CreateContainerCmd build(ServiceDescriptor td, CreateContainerCmd cmd, WebHealthCheck whc) {
		final String serviceNameEnv = SERVICE_PREFIX + whc.exposedPort() + SERVICE_NAME_SUFFIX + "=" + td.getNamed().value();
		final String serviceCheckScriptEnv = SERVICE_CHECK_SCRIPT + "=`which curl` -f " 
				+ (whc.protocol().equals(WebProtocol.HTTPS) ? "-sSk " : "")
				+ whc.protocol().toString().toLowerCase()
				+ "://$SERVICE_IP:$SERVICE_PORT"
				+ whc.endpoint();
		final String serviceCheckIntervalEnv = SERVICE_CHECK_INTERVAL + "=" + whc.pollingInterval() + "s";
		
		List<String> finalEnv = new ArrayList<>();
		List<String> healthCheckEnv = Arrays.asList(serviceNameEnv, serviceCheckScriptEnv, serviceCheckIntervalEnv);
		finalEnv.addAll(healthCheckEnv);

		String[] env = cmd.getEnv();
		
		if(env != null) {
			finalEnv.addAll(Arrays.asList(env));
		}
		return cmd.withEnv(finalEnv);
	}

}
