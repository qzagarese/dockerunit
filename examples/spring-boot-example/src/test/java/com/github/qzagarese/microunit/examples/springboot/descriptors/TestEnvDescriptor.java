package com.github.qzagarese.microunit.examples.springboot.descriptors;

import com.github.qzagarese.dockerunit.annotation.Env;
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck;
import com.github.qzagarese.microunit.examples.springboot.Constants;

import static com.github.qzagarese.microunit.examples.springboot.Constants.*;

@Named(Constants.SERVICE_NAME)
@Image(Constants.IMAGE_NAME)
@WebHealthCheck(exposedPort=8080)
@PortBinding(exposedPort=8080, hostPort=8080)
@Env({"FOO=" + FOO_VALUE_FROM_DESCRIPTOR, "BAR=" + BAR_VALUE_FROM_DESCRIPTOR})
public class TestEnvDescriptor {

}
