package com.github.qzagarese.dockerunit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.qzagarese.dockerunit.annotation.impl.VolumeExtensionInterpreter;

/**
 * 
 * Equivalent of {@literal -v} in docker run.
 * Allows mapping of host files or directories inside 
 * the instantiated containers.
 * 
 * It can be easily leveraged to mount test config during tests execution.
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(Volumes.class)
@ExtensionMarker(VolumeExtensionInterpreter.class)
public @interface Volume {

	/**
	 * If set to true, it tells Dockerunit to treat the host path as a path that is relative to the test classpath.
	 * If the base test path for resources is {@literal src/test/resources},
	 * then a host value of {@literal config/config.json} will be translated 
	 * into an absolute path that points to {@literal /path/to/src/test/resources/config/config.json}.
	 * 
	 * If set to false, the value of host will be treated as an absolute path.
	 * 
	 * @return whether the value of host should be treated as relative to the test classpath.
	 */
    boolean useClasspath() default false;
    
    /**
     * 
     * @return the host path of the volume.
     */
    String host();
    
    
    /**
     * 
     * @return the absolute path where the host volume should be mounted inside the container.
     */
    String container();
    
    /**
     * 
     * @return the access mode to the volume (read write or read only).
     */
    AccessMode accessMode() default AccessMode.RW;
    
    public static enum AccessMode {
        RW, RO
    }
}
