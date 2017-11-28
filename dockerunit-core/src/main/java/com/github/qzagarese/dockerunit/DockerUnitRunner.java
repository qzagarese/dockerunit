package com.github.qzagarese.dockerunit;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.github.qzagarese.dockerunit.discovery.DiscoveryProvider;
import com.github.qzagarese.dockerunit.discovery.DiscoveryProviderFactory;
import com.github.qzagarese.dockerunit.internal.DependencyDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilder;
import com.github.qzagarese.dockerunit.internal.ServiceContextBuilderFactory;
import com.github.qzagarese.dockerunit.internal.lifecycle.InvokeDockerUnitMethod;
import com.github.qzagarese.dockerunit.internal.lifecycle.DockerUnitAfter;
import com.github.qzagarese.dockerunit.internal.lifecycle.DockerUnitAfterClass;
import com.github.qzagarese.dockerunit.internal.lifecycle.DockerUnitBefore;
import com.github.qzagarese.dockerunit.internal.lifecycle.DockerUnitBeforeClass;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilder;
import com.github.qzagarese.dockerunit.internal.reflect.DependencyDescriptorBuilderFactory;

/*
 * Use this runner to enable Dockerunit in your tests.
 * 
 * <pre>
 * {@code 
 * @RunWith(DockerUnitRunner.class)
 * public class MyTestClass {
 * }
 * }
 * </pre>
 * 
 */
public class DockerUnitRunner extends BlockJUnit4ClassRunner {

	private final Map<FrameworkMethod, ServiceContext> methodsContexts = new HashMap<>();
	private ServiceContext classContext;
	private ServiceContext discoveryContext;
	private final DependencyDescriptorBuilder descriptorBuilder = DependencyDescriptorBuilderFactory.create();
	private final ServiceContextBuilder contextBuilder = ServiceContextBuilderFactory.create();
	private final DiscoveryProvider discoveryProvider;
	
	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
	
    public DockerUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
        ServiceLoader<DiscoveryProviderFactory> loader = ServiceLoader.load(DiscoveryProviderFactory.class);
        List<DiscoveryProviderFactory> implementations = new ArrayList<>();
        loader.forEach(impl -> {
        	logger.info("Found discovery provider factory of type " + impl.getClass().getSimpleName());
        	implementations.add(impl);
        });
        if(implementations.size() > 0) {
        	logger.info("Using discovery provider factory " + implementations.get(0).getClass().getSimpleName());
        	discoveryProvider = implementations.get(0).getProvider();
        } else {
        	throw new InitializationError("No discovery provider factory found. Aborting test.");
        }
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return new InvokeDockerUnitMethod(method, test, this);
    }

	@Override
	protected void validateTestMethods(List<Throwable> errors) {
        validatePublicVoidNoOrServiceContextArgMethods(Test.class, false, errors);
	}	

	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
		Statement next = new DockerUnitAfter(method, this, statement, discoveryProvider, contextBuilder);
		return super.withAfters(method, target, next);
	}
	
	@Override
	protected Statement withAfterClasses(Statement statement) {
		Statement next = new DockerUnitAfterClass(this, statement, discoveryProvider, contextBuilder);
		return super.withAfterClasses(next);
	}
	
	
	@Override
	protected Statement withBeforeClasses(Statement statement) {
		Statement next = super.withBeforeClasses(statement);
		DependencyDescriptor descriptor = descriptorBuilder.buildDescriptor(getTestClass().getJavaClass());
		DependencyDescriptor discoveryProviderDescriptor = descriptorBuilder.buildDescriptor(discoveryProvider.getDiscoveryConfig());
		return new DockerUnitBeforeClass(this, next, discoveryProvider, contextBuilder, descriptor, discoveryProviderDescriptor);
	}


	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
		Statement next =  super.withBefores(method, target, statement);
		DependencyDescriptor descriptor = descriptorBuilder.buildDescriptor(method);
		return new DockerUnitBefore(method, this, next, discoveryProvider, contextBuilder, descriptor);
	}
	
	

	protected void validatePublicVoidNoOrServiceContextArgMethods(Class<? extends Annotation> annotation,
            boolean isStatic, List<Throwable> errors) {
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoid(isStatic, errors);
            validateNoOrOneServiceContextArg(eachTestMethod, errors);
        }
	}

	private void validateNoOrOneServiceContextArg(FrameworkMethod testMethod, List<Throwable> errors) {
		if (testMethod.getMethod().getParameterCount() > 0
				&& !testMethod.getMethod().getParameterTypes()[0].isAssignableFrom(ServiceContext.class)) {
			errors.add(new Exception("Test method " + testMethod.getName()
					+ "() must have either zero args or one arg of type " + ServiceContext.class.getName()));
		}
	}

	public void setContext(FrameworkMethod method, ServiceContext context) {
		methodsContexts.put(method, context);
	}
	
	public ServiceContext getContext(FrameworkMethod method) {
		return methodsContexts.get(method);
	}
	
	public void setClassContext(ServiceContext context) {
		this.classContext = context;
	}

	public ServiceContext getClassContext() {
		return classContext;
	}

	public ServiceContext getDiscoveryContext() {
		return discoveryContext;
	}

	public void setDiscoveryContext(ServiceContext discoveryContext) {
		this.discoveryContext = discoveryContext;
	}

}
