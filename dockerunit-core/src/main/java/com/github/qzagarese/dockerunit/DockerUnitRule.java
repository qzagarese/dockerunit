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

    private static final Map<String, ServiceContext> ACTIVE_SERVICE_CONTEXTS = new HashMap<>();
    private static final Map<String, NetworkContext> ACTIVE_NETWORK_CONTEXTS = new HashMap<>();

    private final UsageDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
    private final ServiceContextBuilder serviceContextBuilder = ServiceContextBuilderFactory.create();
    private final NetworkContextBuider networkContextBuilder  = NetworkContextBuilderFactory.create();
    private final DiscoveryProvider discoveryProvider;
    private final String serviceContextName;
    private final String networkContextName;
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
        return Optional.ofNullable(ACTIVE_SERVICE_CONTEXTS.get(serviceContextName))
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
        return Optional.ofNullable(ACTIVE_SERVICE_CONTEXTS.get(GLOBAL_CONTEXT_NAME))
                .orElse(
                        ACTIVE_SERVICE_CONTEXTS.values().stream().findAny()
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
        this(serviceContextName, GLOBAL_CONTEXT_NAME);
    }

    public DockerUnitRule(String serviceContextName, String networkContextName) {
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
        this.networkContextName = networkContextName;
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

        DockerUnitSetup dockerUnitSetup = new DockerUnitSetup(networkContextBuilder, serviceContextBuilder, discoveryProvider);
        NetworkContext networkContext = dockerUnitSetup.setupNetworks(descriptor);
        ServiceContext serviceContext = dockerUnitSetup.setupServices(descriptor);
        
        ACTIVE_SERVICE_CONTEXTS.put(this.serviceContextName, serviceContext);
        ACTIVE_NETWORK_CONTEXTS.put(this.networkContextName, networkContext);
        if (!serviceContext.checkStatus(Status.DISCOVERED)) {
            throw new RuntimeException(serviceContext.getFormattedErrors());
        }
        
    }

	
    private void doTeardown() {
        ServiceContext serviceContext = ACTIVE_SERVICE_CONTEXTS.get(this.serviceContextName);
        if (serviceContext != null) {
            ServiceContext cleared = serviceContextBuilder.clearContext(serviceContext);
            discoveryProvider.clearRegistry(cleared, new DefaultServiceContext(new HashSet<>()));
        }

        NetworkContext networkContext = ACTIVE_NETWORK_CONTEXTS.get(this.networkContextName);
        if(networkContext != null) {
            networkContextBuilder.clearContext(networkContext);
        }
        
        if (this.discoveryContext != null) {
            serviceContextBuilder.clearContext(discoveryContext);
        }
    }

    private void runTest(final Statement base) throws Throwable {
        base.evaluate();
    }

}
