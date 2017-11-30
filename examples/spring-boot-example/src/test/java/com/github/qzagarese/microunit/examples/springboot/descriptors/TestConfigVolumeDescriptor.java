package com.github.qzagarese.microunit.examples.springboot.descriptors;

import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.annotation.Volume;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck;
import com.github.qzagarese.microunit.examples.springboot.Constants;

@Named(Constants.SERVICE_NAME)
@Image(Constants.IMAGE_NAME)
@WebHealthCheck(exposedPort=8080)
@PortBinding(exposedPort=8080, hostPort=8080)
@Volume(host="test.properties", container="/application.properties", useClasspath=true)
public class TestConfigVolumeDescriptor {

}
