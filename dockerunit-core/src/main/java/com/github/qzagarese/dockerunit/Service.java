package com.github.qzagarese.dockerunit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;


/**
 * 
 * Represents a discoverable container or group of Docker containers.
 * Each container is based on the declared image.
 * @see Image
 * 
 * It has at least one instance (container) and it is healthy if and only
 * if all its instances are healthy.
 * 
 * The service name is used to instruct the {@link DiscoveryProvider} and 
 * make the service discoverable by  other services.
 * @see Named
 *
 */
@Wither
@AllArgsConstructor
public class Service {

    private final String name;
    private final Set<ServiceInstance> instances;
    
    /**
     *  
     * @return true if all the instances have started successfully.
     */
    public boolean isHealthy() {
    	return !instances.stream()
    			.filter(this::isAborted)
    			.findFirst().isPresent();
    }

    /**
     * 
     * @return the service name as declared in {@linkplain Named}
     */
    public String getName() {
    	return this.name;
    }
    
    /**
     * 
     * @return the set of instances (containers) for this service.
     * @see ServiceInstance
     */
    public Set<ServiceInstance> getInstances() {
    	return this.instances;
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
