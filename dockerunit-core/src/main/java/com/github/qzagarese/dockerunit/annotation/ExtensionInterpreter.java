package com.github.qzagarese.dockerunit.annotation;

import java.lang.annotation.Annotation;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.internal.ServiceDescriptor;

/**
 * 
 * Definition of an annotation interpreter.
 * Extending Dockerunit consists of:
 * 1) Declaring an annotation
 * 2) Marking it with &#64;ExtensionMarker
 * 3) Providing an interpreter for the created annotation 
 * 		that applies changes to the Docker container based on the annotation value.
 *
 * <pre> 
 * {@code 
 *  [at]ExtensionMarker(MyDnsAnnotationInterpreter.class)
 * 	public @interface MyDnsAnnotation {
 * 		String value() default "8.8.4.4";
 *  }
 *  
 *  public class MyDnsAnnotationInterpreter implements ExtensionInterpreter<MyDnsAnnotation> {
 *  
 *    [at]Override
 *    public CreateContainerCmd build(TestDescriptor td, CreateContainerCmd cmd, MyDnsAnnotation t) {
 *      return cmd.withDns(t.value());
 *    }
 *  }
 * }
 * </pre>
 * @param <T> the type variable indicating the annotation this class can interpret.
 */
public interface ExtensionInterpreter<T extends Annotation> {

	/**
	 * Updates the {@linkplain CreateContainerCmd} instance to reflect the value of the interpreted annotation.
	 * 
	 * @param td the whole configuration for this test including values from other annotations.
	 * @param cmd the docker-java command that will instantiate the Docker container based on Dockerunit config
	 * @param t the annotation that is interpreted by this class
	 * @return the updated {@linkplain CreateContainerCmd} instance
	 */
    CreateContainerCmd build(ServiceDescriptor td, CreateContainerCmd cmd, T t);
    
}
