package com.github.qzagarese.dockerunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilderFactory;
import com.github.qzagarese.dockerunit.internal.reflect.UsageDescriptorBuilder;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilderFactory;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

public class DockerUnitRule implements TestRule {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private static final Map<String, ServiceContext> activeContexts = new HashMap<>();

    private final UsageDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
    private final ServiceContextBuilder contextBuilder = ServiceContextBuilderFactory.create();
    private final DiscoveryProvider discoveryProvider;
    private final String serviceContextName;
    private ServiceContext discoveryContext;

    
    public static ServiceContext getServiceContext(String serviceContextName) {
        return activeContexts.get(serviceContextName);
    }
    
    public static ServiceContext getDefaultServiceContext() {
        return activeContexts.values().stream().findAny().orElse(null);
    }
    
    public DockerUnitRule(String serviceContextName) {
        ServiceLoader<DiscoveryProviderFactory> loader = ServiceLoader.load(DiscoveryProviderFactory.class);
        List<DiscoveryProviderFactory> implementations = new ArrayList<>();
        loader.forEach(impl -> {
            logger.info("Found discovery provider factory of type " + impl.getClass().getSimpleName());
            implementations.add(impl);
        });
        if (implementations.size() > 0) {
            logger.info("Using discovery provider factory " + implementations.get(0).getClass().getSimpleName());
            discoveryProvider = implementations.get(0).getProvider();
        } else {
            throw new RuntimeException("No discovery provider factory found. Aborting test.");
        }
        this.serviceContextName = serviceContextName;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                
                logger.log(Level.INFO, "Performing service discovery for the following context: " + description.getDisplayName());
                try {
                    doSetup(description);
                    base.evaluate(); // This will run the test.
                } finally {
                    logger.log(Level.INFO, "Cleaning up active services for the following context: " + description.getDisplayName());
                    doTeardown();
                }
            }
        };
    }

    private void doSetup(final Description description) {
        UsageDescriptor descriptor = descriptorBuilder.buildDescriptor(description.getTestClass());
        UsageDescriptor discoveryProviderDescriptor = descriptorBuilder.buildDescriptor(discoveryProvider.getDiscoveryConfig());
      
        // Build discovery context
        ServiceContext discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
        DockerUnitRule.this.discoveryContext = discoveryContext;
        if(!discoveryContext.allHealthy()) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }

        
        // Create containers and perform discovery one service at the time
        List<ServiceContext> serviceContexts = new ArrayList<>();
        descriptor.getDependencies().forEach(usage -> {
            ServiceContext context = contextBuilder.buildServiceContext(usage);
            if(!context.allHealthy()) {
                throw new RuntimeException(context.getFormattedErrors());
            }
            context = discoveryProvider.populateRegistry(context);
            serviceContexts.add(context);
        });
        ServiceContext completeContext = mergeContexts(serviceContexts);
        activeContexts.put(DockerUnitRule.this.serviceContextName, completeContext);
        if(!completeContext.allHealthy()) {
            throw new RuntimeException(completeContext.getFormattedErrors());
        }
        
    }

    private ServiceContext mergeContexts(List<ServiceContext> serviceContexts) {
        ServiceContext completeContext = null;
        if (serviceContexts.size() > 0) {
            completeContext = serviceContexts.remove(0);
        }
        for (ServiceContext serviceContext : serviceContexts) {
            completeContext = completeContext.merge(serviceContext);
        }
        return completeContext;
    }

    private void doTeardown() {
        ServiceContext context = activeContexts.get(DockerUnitRule.this.serviceContextName);
        if(context != null) {
            ServiceContext cleared = contextBuilder.clearContext(context);
            discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
        }
        
        ServiceContext discoveryContext = DockerUnitRule.this.discoveryContext;
        if(discoveryContext != null) {  
            contextBuilder.clearContext(discoveryContext);
        }
    }

}
