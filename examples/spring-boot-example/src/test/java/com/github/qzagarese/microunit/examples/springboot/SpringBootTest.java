package com.github.qzagarese.microunit.examples.springboot;

import static com.github.qzagarese.microunit.examples.springboot.Constants.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.github.qzagarese.microunit.examples.springboot.descriptors.BaseDescriptor;
import com.github.qzagarese.microunit.examples.springboot.descriptors.TestConfigVolumeDescriptor;
import com.github.qzagarese.microunit.examples.springboot.descriptors.TestEnvDescriptor;
import com.jayway.restassured.RestAssured;
import static org.hamcrest.Matchers.equalTo;

@RunWith(DockerUnitRunner.class)
public class SpringBootTest {

	@Test
	@Use(service=BaseDescriptor.class)
	public void healthCheckShouldReturn200(ServiceContext context) {
		Service s = context.getService(SERVICE_NAME);
		ServiceInstance si = s.getInstances().stream().findAny().get();
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/health-check")
			.then()
				.assertThat()
				.statusCode(200);
	}
	
	@Test
	@Use(service=BaseDescriptor.class)
	public void greetingShouldReturnImageConfigValue(ServiceContext context) {
		Service s = context.getService(SERVICE_NAME);
		ServiceInstance si = s.getInstances().stream().findAny().get();
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/greeting")
			.then()
				.assertThat()
				.statusCode(200)
				.and()
				.body("greeting", equalTo("Hello world!"));
	}
	
	@Test
	@Use(service=TestConfigVolumeDescriptor.class)
	public void greetingShouldReturnTestConfigValue(ServiceContext context) {
		Service s = context.getService(SERVICE_NAME);
		ServiceInstance si = s.getInstances().stream().findAny().get();
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/greeting")
			.then()
				.assertThat()
				.statusCode(200)
				.and()
				.body("greeting", equalTo("Hello Dockerunit!!!"));
	}
	
	@Test
	@Use(service=BaseDescriptor.class)
	public void envShouldReturnValuesFromImage(ServiceContext context) {
		Service s = context.getService(SERVICE_NAME);
		ServiceInstance si = s.getInstances().stream().findAny().get();
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/env/foo")
			.then()
				.assertThat()
				.statusCode(200)
				.and()
				.body("value", equalTo(FOO_VALUE_FROM_IMAGE));
	}
	
	@Test
	@Use(service=TestEnvDescriptor.class)
	public void envShouldReturnValuesFromDescriptor(ServiceContext context) {
		Service s = context.getService(SERVICE_NAME);
		ServiceInstance si = s.getInstances().stream().findAny().get();
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/env/bar")
			.then()
				.assertThat()
				.statusCode(200)
				.and()
				.body("value", equalTo(BAR_VALUE_FROM_DESCRIPTOR));
	}
	
}
