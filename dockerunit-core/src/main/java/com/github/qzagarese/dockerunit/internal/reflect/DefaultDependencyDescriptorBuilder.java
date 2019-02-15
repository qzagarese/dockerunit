package com.github.qzagarese.dockerunit.internal.reflect;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.qzagarese.dockerunit.annotation.*;
import com.github.qzagarese.dockerunit.internal.NetworkDescriptor;
import com.github.qzagarese.dockerunit.internal.ResourceDescriptor;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;
import com.github.qzagarese.dockerunit.internal.UsageDescriptor;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultDependencyDescriptorBuilder implements UsageDescriptorBuilder {

    @Override
    public UsageDescriptor buildDescriptor(FrameworkMethod method) {
       return buildDescriptor((AnnotatedElement) method.getMethod());
    }

    
    @Override
    public UsageDescriptor buildDescriptor(Class<?> klass) {
    	 return buildDescriptor((AnnotatedElement)klass);
     }
    
    private UsageDescriptor buildDescriptor(AnnotatedElement element) {
    	List<Use> requirements = getDependencies(element);
        List<ResourceDescriptor> descriptors = asDescriptors(requirements);
        return new DefaultDependencyDescriptor(descriptors);
    }
    
    private List<ResourceDescriptor> asDescriptors(List<Use> requirements) {
        List<ResourceDescriptor> descriptors = requirements.stream()
            .map(use -> buildDescriptor(use))
            .collect(Collectors.toList());
        return descriptors;
    }

    private ResourceDescriptor buildDescriptor(Use use) {
        if (isNetworkDescriptor(use.service()) {
            return buildNetworkDescriptor(use);
        }
        return buildServiceDescriptor(use);
    }

    private ServiceDescriptor buildServiceDescriptor(Use use) {
        Object resourceInstance = instantiateResourceClass(use.service());
        return DefaultServiceDescriptor.builder()
                .instance(resourceInstance)
                .named(findNamed(use.service()))
                .customisationHook(findCustomisationHook(use, CreateContainerCmd.class))
                .options(extractOptions(use.service()))
                .order(use.order())
                .replicas(extractReplicas(use))
                .containerName(use.containerPrefix())
                .image(findImage(use.service()))
                .build();
    }

    private NetworkDescriptor buildNetworkDescriptor(Use use) {
        Object resourceInstance = instantiateResourceClass(use.service());
        return DefaultNetworkDescriptor.builder()
                .instance(resourceInstance)
                .named(findNamed(use.service()))
                .order(use.order())
                .customisationHook(findCustomisationHook(use, CreateNetworkCmd.class))
                .build();
    }

    private Object instantiateResourceClass(Class<?> clazz) {
        checkResourceClass(clazz);
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate resource class " + use.service().getName());
        }
    }

    private boolean isNetworkDescriptor(Class<?> service) {
        return service.isAnnotationPresent(NetworkDefinition.class);
    }

    private List<? extends Annotation> extractOptions(Class<?> service) {
        return Arrays.asList(service.getDeclaredAnnotations()).stream()
            .filter(a -> a.annotationType().isAnnotationPresent(ExtensionMarker.class))
            .collect(Collectors.toList());
    }

    private void checkResourceClass(Class<?> service) {
    	String className = service.getSimpleName();
        StringBuffer buffer = new StringBuffer();
        if(service.isInterface()) {
            buffer.append(className + " cannot be an interface. ");
        }
        if(service.isEnum()) {
            buffer.append(className + " cannot be an enum. ");
        }
        if(service.isSynthetic()) {
            buffer.append(className + " cannot be synthetic. ");
        }
        if(service.isAnnotation()) {
            buffer.append(className + " cannot be an annotation. ");
        }
        if(service.isArray()) {
            buffer.append(className + " cannot be an array. ");
        }
        if(service.isAnonymousClass()) {
            buffer.append(className + " cannot be anonymous. ");
        }
        if(service.isLocalClass()) {
            buffer.append(className + " cannot be a local class. ");
        }
        if(service.isMemberClass()) {
            buffer.append(className + " cannot be a member class. ");
        }
        int modifiers = service.getModifiers();
        if(Modifier.isAbstract(modifiers)) {
            buffer.append("Service class cannot be abstract. ");
        }
        if(!Modifier.isPublic(modifiers)) {
            buffer.append("Service class must be public. ");
        }
        if(Modifier.isStatic(modifiers)) {
            buffer.append("Service class cannot be static. ");
        }
        Optional<Constructor<?>> c = findSuitableContructor(service.getDeclaredConstructors());
        if (!c.isPresent()) {
            buffer.append("Service class must provide a public zero args constructor. ");
        }
        
        if(buffer.length() > 0) {
            throw new RuntimeException(buffer.toString());
        }
        
    }

    private Optional<Constructor<?>> findSuitableContructor(Constructor<?>[] declaredConstructors) {
        return Arrays.asList(declaredConstructors)
            .stream()
            .filter(c -> Modifier.isPublic(c.getModifiers()) && c.getParameterCount() == 0)
            .findFirst();
    }

    private Method findCustomisationHook(Use use, Class<?> commandType) {
        Optional<Method> opt = Arrays.asList(use.service().getDeclaredMethods()).stream()
            .filter(m -> m.isAnnotationPresent(ContainerBuilder.class)
                && Modifier.isPublic(m.getModifiers())
                && !Modifier.isStatic(m.getModifiers())
                && m.getParameterCount() == 1 
                && m.getParameterTypes()[0].equals(commandType)
                && m.getReturnType().equals(commandType))
            .findFirst();
        return opt.orElseGet(() -> null);
    }

    private int extractReplicas(Use use) {
        if(use.replicas() < 1) {
            throw new RuntimeException("Cannot require less than one replica");
        }
        return use.replicas();
    }

    private <T extends Annotation> T findRequiredAnnotation(Class<?> service, Class<T> annotationType) {
    	if(!service.isAnnotationPresent(annotationType)) {
    		throw new RuntimeException("No @" + annotationType.getSimpleName() + " has been specified on class " + service.getName());
    	}
    	return service.getAnnotation(annotationType);
    }
    
    private Image findImage(Class<?> service) {
        return findRequiredAnnotation(service, Image.class);
    }

    private Named findNamed(Class<?> service) {
    	return findRequiredAnnotation(service, Named.class);
    }
    
    private List<Use> getDependencies(AnnotatedElement element) {
        Use[] requirements = element.isAnnotationPresent(Usages.class)
                    ? element.getAnnotation(Usages.class)
                        .value()
                    : new Use[] {};
        if(requirements.length == 0 && element.isAnnotationPresent(Use.class)) {
        	requirements = new Use[] {element.getAnnotation(Use.class)};
        }                
        return Arrays.asList(requirements);
    }

}
