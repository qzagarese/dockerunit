package com.github.qzagarese.dockerunit.internal.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

public class DefaultDockerClientProvider implements DockerClientProvider {

    private static final String DOCKER_SERVER_URL = "docker.server.url";
    private static final String DOCKER_SOCKET = "unix:///var/run/docker.sock";
    private final DockerClient client;
    
    
    public DefaultDockerClientProvider() {
        String dockerServerUrl = System.getProperty(DOCKER_SERVER_URL, DOCKER_SOCKET);
        client = DockerClientBuilder.getInstance(dockerServerUrl).build();
    }
    
    @Override
    public DockerClient getClient() {
        return client;
    }
    
}
