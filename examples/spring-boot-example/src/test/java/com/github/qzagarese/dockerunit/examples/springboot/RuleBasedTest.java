
package com.github.qzagarese.dockerunit.examples.springboot;

import static com.github.qzagarese.dockerunit.examples.springboot.Constants.SERVICE_NAME;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.qzagarese.dockerunit.DockerUnitRule;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.github.qzagarese.dockerunit.examples.springboot.descriptors.BaseDescriptor;
import com.jayway.restassured.RestAssured;

@Category(ContainerTest.class)
@Use(service=BaseDescriptor.class, replicas = 2)
public class RuleBasedTest {
	
	@Rule
	public DockerUnitRule rule = new DockerUnitRule();
	
	private ServiceContext context;
	
	@Before
	public void setup() {
		context = DockerUnitRule.getDefaultServiceContext();
	}
	
	@Test
	public void healthCheckShouldReturn200() {
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
	public void healthCheckShouldReturn200FromEachReplica() {
		Service s = context.getService(SERVICE_NAME);
		s.getInstances().forEach(si -> {
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort())
			.when()
				.get("/health-check")
			.then()
				.assertThat()
				.statusCode(200);
		});
	}
	
	@Test
	public void greetingShouldReturnImageConfigValue() {
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
	
}
