package com.github.qzagarese.dockerunit;

import com.github.qzagarese.dockerunit.ServiceInstance.Status;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;
import com.github.qzagarese.dockerunit.exception.ConfigException;
import com.github.qzagarese.dockerunit.internal.*;
import com.github.qzagarese.dockerunit.internal.lifecycle.DockerUnitSetup;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilderFactory;
import com.github.qzagarese.dockerunit.internal.reflect.UsageDescriptorBuilder;
import com.github.qzagarese.dockerunit.internal.service.DefaultServiceContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class DockerUnitRule implements TestRule {

    private static final Logger logger = Logger.getLogger(DockerUnitRule.class.getSimpleName());

    private static final Map<String, ServiceContext> activeContexts = new HashMap<>();

    private final UsageDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
    private final ServiceContextBuilder serviceContextBuilder = ServiceContextBuilderFactory.create();
    private final NetworkContextBuider networkContextBuilder  = NetworkContextBuilderFactory.create();
    private final DiscoveryProvider discoveryProvider;
    private final String serviceContextName;
    private ServiceContext discoveryContext;
    
    private static final String GLOBAL_CONTEXT_NAME = "dockerunit_global_context";    

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
     * Returns the default {@link ServiceContext} or a random one if all the current instances of {@link DockerUnitRule}
     * have been initialised with a specific service context name.
     * 
     * @return the default{@link ServiceContext}
     */
    public static ServiceContext getDefaultServiceContext() {
        return Optional.ofNullable(activeContexts.get(GLOBAL_CONTEXT_NAME))
                .orElse(
                        activeContexts.values().stream().findAny()
                                .orElseThrow(() -> new ConfigException("No active context detected. "
                                    + "Please make sure that you have declared at least one @"
                                    + Use.class.getSimpleName()
                                    + " annotation on tha class that declares your "
                                    + DockerUnitRule.class.getSimpleName()
                                    + " instance.")));
    }
    
    public DockerUnitRule() {
        this(GLOBAL_CONTEXT_NAME);
    }
    
    public DockerUnitRule(String serviceContextName) {
        ServiceLoader<DiscoveryProviderFactory> loader = ServiceLoader.load(DiscoveryProviderFactory.class);
       
        this.discoveryProvider = StreamSupport.stream(loader.spliterator(), false)
                .peek(impl -> logger.info("Found discovery provider factory of type " + impl.getClass().getSimpleName()))
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
      
        // Build service discovery context
        this.discoveryContext = serviceContextBuilder.buildContext(discoveryProviderDescriptor);
        if (!discoveryContext.checkStatus(Status.STARTED)) {
            throw new RuntimeException(discoveryContext.getFormattedErrors());
        }
        
        ServiceContext completeContext = new DockerUnitSetup(serviceContextBuilder, discoveryProvider).setup(descriptor);
        
        activeContexts.put(this.serviceContextName, completeContext);
        if (!completeContext.checkStatus(Status.DISCOVERED)) {
            throw new RuntimeException(completeContext.getFormattedErrors());
        }
        
    }

	
    private void doTeardown() {
        ServiceContext context = activeContexts.get(this.serviceContextName);
        if (context != null) {
            ServiceContext cleared = serviceContextBuilder.clearContext(context);
            discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
        }
        
        if (this.discoveryContext != null) {
            serviceContextBuilder.clearContext(discoveryContext);
        }
    }

    private void runTest(final Statement base) throws Throwable {
        base.evaluate();
    }

}
