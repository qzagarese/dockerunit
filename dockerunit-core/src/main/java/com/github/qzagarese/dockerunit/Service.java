package com.github.qzagarese.dockerunit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;


/**
 * 
 * Represents a discoverable container or group of Docker containers.
 * Each container is based on the declared image.
 * 
 * It has at least one instance (container) and it is healthy if and only
 * if all its instances are healthy.
 * 
 * The service name is used to instruct the {@link DiscoveryProvider} and 
 * make the service discoverable by  other services.

 * @see Image
 * @see Named
 *
 */
@Wither
@AllArgsConstructor
public class Service {

    private final String name;
    private final Set<ServiceInstance> instances;
    private final ServiceDescriptor descriptor;
    
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
     * Checks whether all the {@link ServiceInstance}s in this service are in the specified {@link Status}.
     * 
     * @param status the {@link Status} to check
     * @return true if all the {@link ServiceInstance}s in this service are in the specified status, false otherwise.
     */
    public boolean checkStatus(Status status) {
        return instances.stream()
                .allMatch(si -> si.getStatus().equals(status));
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
    
    
    /**
     * 
     * @return the descriptor of this service that provides the runtime representation of the annotation based
     * configuration that has been used.
     */
    public ServiceDescriptor getDescriptor() {
        return this.descriptor;
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
