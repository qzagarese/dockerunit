package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Connects your test to one of service descriptors that you have defined.
 * It basically tells Dockerunit which services you need in your test.
 * 
 * It can be used at class and method level.
 * If used at class level, the referenced services will be started before JUnit 
 * executes your &#64;BeforeClass method.
 * 
 * If used at method level, the referenced services will be started before JUnit 
 * executes your &#64;Before method.
 * 
 * It can also be used at class and method level at the same time, if some
 * services will be used by all the tests in your class, 
 * while other are specific for one method.
 *
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Repeatable(Usages.class)
public @interface Use {

	/**
     * @deprecated Use resource() instead.
     *
	 * The descriptor class that defines how to instantiate the service
	 *
	 * @return the descriptor type
	 */
    Class<?> service();


    /**
     *
     * The descriptor class that defines how to instantiate the resource
     *
     * @return the descriptor type
     */
    Class<?> resource();

    
    /**
     * A prefix that will be used to give names to the containers instantiated
     * to run your service. 
     * 
     * If not set, Docker random names will be used.
     * If set and replicas has been set to 1, the actual value will be the container name.
     * If set and replicas has been set to more than 1, containers will be named as value + "1", value + "2", ... 
     * 
     * @return the value to set as name prefix for containers instantiated for this service.
     */
    String containerPrefix() default "";
    
    /**
     * 
     * @return the number of replicas that should be instantiated for this service
     */
    int replicas() default 1;
    
    /**
     * Value used to support dependencies between services. 
     * The lower the value, the earlier this service will be instantiated.
     * The higher the value, the later this service will be instantiated.
     * 
     * @return the order for this service
     */
    int order() default 0;
    
}
