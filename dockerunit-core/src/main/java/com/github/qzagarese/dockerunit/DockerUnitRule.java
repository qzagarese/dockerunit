package com.github.qzagarese.dockerunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;
import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilderFactory;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilder;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilderFactory;

public class DockerUnitRule implements TestRule {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private static final Map<String, ServiceContext> activeContexts = new HashMap<>();

    private final DependencyDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
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
    
    public DockerUnitRule(String serviceContextName) throws InitializationError {
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
            throw new InitializationError("No discovery provider factory found. Aborting test.");
        }
        this.serviceContextName = serviceContextName;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                
                logger.log(Level.INFO, "Performing service discovery for the following context: " + description.getDisplayName());
                doSetup(description);
                try {
                    base.evaluate(); // This will run the test.
                } finally {
                    logger.log(Level.INFO, "Cleaning up active services for the following context: " + description.getDisplayName());
                    doTeardown();
                }
            }
        };
    }

    private void doSetup(final Description description) {
        DependencyDescriptor descriptor = descriptorBuilder.buildDescriptor(description.getTestClass());
        DependencyDescriptor discoveryProviderDescriptor = descriptorBuilder.buildDescriptor(discoveryProvider.getDiscoveryConfig());
      
        ServiceContext discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
        DockerUnitRule.this.discoveryContext = discoveryContext;
        if(!discoveryContext.allHealthy()) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }
        
        ServiceContext context = contextBuilder.buildContext(descriptor);
        activeContexts.put(DockerUnitRule.this.serviceContextName, context);
        if(!context.allHealthy()) {
            throw new RuntimeException(context.getFormattedErrors());
        }
        
        context = discoveryProvider.populateRegistry(context);
        activeContexts.put(DockerUnitRule.this.serviceContextName, context);
        if(!context.allHealthy()) {
            throw new RuntimeException(context.getFormattedErrors());
        }
    }

    private void doTeardown() {
        ServiceContext context = activeContexts.get(DockerUnitRule.this.serviceContextName);
        if(context != null) {
            ServiceContext cleared = contextBuilder.clearContext(context);
            discoveryProvider.clearRegistry(cleared, cleared);
        }
        
        ServiceContext discoveryContext = DockerUnitRule.this.discoveryContext;
        if(discoveryContext != null) {  
            contextBuilder.clearContext(discoveryContext);
        }
    }

}
