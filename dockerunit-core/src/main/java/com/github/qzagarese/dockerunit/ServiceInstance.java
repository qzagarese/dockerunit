package com.github.qzagarese.dockerunit;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@Builder
@Wither
@EqualsAndHashCode(of = {"containerId"})
public class ServiceInstance {

    private final String containerName;
    private final String ip;
    private final int port;
    private String containerId;
    private String statusDetails;
    private Status status;
    
    public static enum Status {
    	STARTED, DISCOVERED, ABORTED, TERMINATED, TERMINATION_FAILED; 	
    }
}
