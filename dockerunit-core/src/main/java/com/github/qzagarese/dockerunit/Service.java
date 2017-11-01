package com.github.qzagarese.dockerunit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.ServiceInstance.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@AllArgsConstructor
@Wither
public class Service {

    private final String name;
    private final Set<ServiceInstance> instances;
    
    public boolean isHealthy() {
    	return !instances.stream()
    			.filter(this::isAborted)
    			.findFirst().isPresent();
    }

    public List<String> getWarnings() {
    	return instances.stream()
    			.filter(this::isAborted)
    			.map(i -> i.getStatusDetails())
    			.collect(Collectors.toList());
    }
        
    private boolean isAborted(ServiceInstance i) {
    	return i.getStatus().equals(Status.ABORTED);
    }
    
}
