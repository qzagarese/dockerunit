package com.github.qzagarese.dockerunit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;
import com.github.qzagarese.dockerunit.exception.ConfigException;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilderFactory;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilderFactory;
import com.github.qzagarese.dockerunit.internal.reflect.UsageDescriptorBuilder;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;

public class DockerUnitRule implements TestRule {

    private static final Logger logger = Logger.getLogger(DockerUnitRule.class.getSimpleName());

    private static final Map<String, ServiceContext> activeContexts = new HashMap<>();

    private final UsageDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
    private final ServiceContextBuilder contextBuilder = ServiceContextBuilderFactory.create();
    private final DiscoveryProvider discoveryProvider;
    private final String serviceContextName;
    private ServiceContext discoveryContext;

    /**
     * Returns the {@link ServiceContext} that has been associated to the instance of {@link DockerUnitRule} that
     * has serviceContextName as parameter.
     * 
     * @param serviceContextName
     * @return the associated {@link ServiceContext}
     */
    public static ServiceContext getServiceContext(String serviceContextName) {
        return Optional.ofNullable(activeContexts.get(serviceContextName))
                .orElseThrow(() -> new ConfigException("No active context for name " + serviceContextName 
                        + " . Please make sure that you are using the context name that you have passed to the "
                        + DockerUnitRule.class.getSimpleName() 
                        + " constructor."));
    }
    
    /**
     * Return a random {@link ServiceContext}, however you will most often have only one, 
     * as contexts are mapped one-to-one to {@link DockerUnitRule} instances. 
     * 
     * @return a random {@link ServiceContext}
     */
    public static ServiceContext getDefaultServiceContext() {
        return activeContexts.values().stream().findAny()
                .orElseThrow(() -> new ConfigException("No active context detected. "
                        + "Please make sure that you have declared at least one @"
                        + Use.class.getSimpleName()
                        + " annotation on tha class that declares your "
                        + DockerUnitRule.class.getSimpleName()
                        + " instance."));
    }
    
    public DockerUnitRule(String serviceContextName) {
        ServiceLoader<DiscoveryProviderFactory> loader = ServiceLoader.load(DiscoveryProviderFactory.class);
       
        this.discoveryProvider = StreamSupport.stream(loader.spliterator(), false)
                .map(impl -> {
                    logger.info("Found discovery provider factory of type " + impl.getClass().getSimpleName());
                    return impl;
                })
                .findFirst()
                .map(impl -> {
                    logger.info("Using discovery provider factory " + impl.getClass().getSimpleName());
                    return impl;
                })
                .map(DiscoveryProviderFactory::getProvider)
                .orElseThrow(() -> new RuntimeException("No discovery provider factory found. Aborting test."));
        
        this.serviceContextName = serviceContextName;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                
                logger.info("Performing service discovery for the following context: " + description.getDisplayName());
                try {
                    doSetup(description);
                    runTest(base); 
                } finally {
                    logger.info("Cleaning up active services for the following context: " + description.getDisplayName());
                    doTeardown();
                }
            }
        };
    }

    private void doSetup(final Description description) {
        UsageDescriptor descriptor = descriptorBuilder.buildDescriptor(description.getTestClass());
        UsageDescriptor discoveryProviderDescriptor = descriptorBuilder.buildDescriptor(discoveryProvider.getDiscoveryConfig());
      
        // Build discovery context
        this.discoveryContext = contextBuilder.buildContext(discoveryProviderDescriptor);
        if(!discoveryContext.checkStatus(Status.STARTED)) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }

        
        // Create containers and perform discovery one service at the time
        List<ServiceContext> serviceContexts = descriptor.getDependencies().stream()
            .map(contextBuilder::buildServiceContext)
            .map(ctx -> {
                if (!ctx.checkStatus(Status.STARTED)) {
                    throw new RuntimeException(ctx.getFormattedErrors());
                }
                logger.info("Performing discovery for service " + ctx.getServices().stream().findFirst().get().getName());
                return discoveryProvider.populateRegistry(ctx);
            })
            .collect(Collectors.toList());  
        
        ServiceContext completeContext = mergeContexts(serviceContexts);
        activeContexts.put(this.serviceContextName, completeContext);
        if(!completeContext.checkStatus(Status.DISCOVERED)) {
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
        ServiceContext context = activeContexts.get(this.serviceContextName);
        if (context != null) {
            ServiceContext cleared = contextBuilder.clearContext(context);
            discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
        }
        
        if (this.discoveryContext != null) {  
            contextBuilder.clearContext(discoveryContext);
        }
    }

    private void runTest(final Statement base) throws Throwable {
        base.evaluate();
    }

}
