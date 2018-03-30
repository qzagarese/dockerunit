package com.github.qzagarese.dockerunit.examples.springboot.descriptors;

import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.annotation.PublishPorts;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck;
import com.github.qzagarese.dockerunit.examples.springboot.Constants;

@Named(Constants.SERVICE_NAME)
@Image(Constants.IMAGE_NAME)
@WebHealthCheck(exposedPort=8080)
@PublishPorts
public class BaseDescriptor {
	
}
