# Dockerunit - JUnit for Docker containers

Dockerunit is an extensible framework for testing of dockerised services and 
applications.
It is based on JUnit and it allows linking of Docker images to Java tests by 
means of Java annotations.
You can think of Dockerunit as of a docker-compose for Java tests.

Dockerunit leverages useful tools like 
[docker-java](https://github.com/docker-java/docker-java), 
[Consul](https://www.consul.io/) and 
[registrator](https://github.com/gliderlabs/registrator) to provide the 
following main features:
1. Automatic pull of images referencing a registry.
2. Service discovery based on Consul + registrator (alternative discovery 
providers can be plugged in).
3. Container port mapping.
4. Volume mapping allowing relative paths from test classpath, so you can 
easily mount test config files.
5. Support for multiple instances of a service, so you can test how your 
services would work on environments like [Kubernetes](https://kubernetes.io/).
6. A simple mechanism similar to the 
[Java Validation Framework](https://jcp.org/en/jsr/detail?id=303) for you to 
add your own annotations.
 
## Usage
You can enable Dockerunit by adding the following dependencies to you POM file 
(set `dockerunit.version` property to the version you intend to use).
```xml
<dependency>
  <groupId>com.github.qzagarese</groupId>
  <artifactId>dockerunit-core</artifactId>
  <version>${dockerunit.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.github.qzagarese</groupId>
  <artifactId>dockerunit-consul</artifactId>
  <version>${dockerunit.version}</version>
  <scope>test</scope>
</dependency>
```

## How it works
Building tests with Dockerunit consists of two main steps:
1. Defining your service descriptor.
2. Using your service from your test.

### 1. Defining your service descriptor
A service descriptor is a class that instructs Dockerunit about how to create 
Docker containers, given a Docker image that you have previously created.
Here is a simple descriptor for a Spring service that is listening on port 8080.

```java
import com.github.qzagarese.dockerunit.annotation.Image;
import com.github.qzagarese.dockerunit.annotation.Named;
import com.github.qzagarese.dockerunit.annotation.PortBinding;
import com.github.qzagarese.dockerunit.discovery.consul.annotation.WebHealthCheck;

// Gives a name to your service. Consul will put a dns entry on `my-spring-service.service.consul`.
@Named("my-spring-service")

// Selects the Docker image to use. It can contain a registry name.
@Image("my-spring-service-image:latest")      

/* Maps the container port 8080 on the same host port number 
(equivalent to `docker run -p 8080:8080 my-spring-service-image:latest`) */ 
@PortBinding(exposedPort=8080, hostPort=8080) 

/* Tells Consul how to monitor the state of your service. 
You should always provide a health check endpoint. 
If not, Dockerunit cannot guarantee that your service has started successfully, 
before your test invokes its endpoints. */  
@WebHealthCheck(exposedPort=8080, endpoint="/health-check")
public class MyServiceDescriptor {
}
```

### 2. Using your service from your test

There are two ways to enable Dockerunit in your tests:

1. As a runner using the `@RunWith` annotation as follows: 
`@RunWith(DockerUnitRunner.class)`.
2. As a rule using the `@Rule` or the `@ClassRule` annotation.  

Using the runner allows you to combine startup of containers at a class and a 
test execution level.
This means that you can start one or more services only once, if they are used 
by all the tests in your test class, 
and at the same time you can spin up the remaining ones when the test that 
needs them is going to execute.

On the other hand, you can only use one runner at the time, so you cannot use 
Dockerunit in conjunction with other testing frameworks if you are using this 
approach.

Using a rule allows you to perform service startup/discovery either once per 
test class execution (`@ClassRule`) or before each test (`@Rule`). 

Moreover, you can use rules with JUnit `@SuiteClasses`, which allows you to 
perform service startup/discovery once and then execute several test classes 
rapidly.

Finally, using a rule allows you to combine Dockerunit with other runner-based 
testing frameworks. 


It's now time to write an actual test.
The following examples use RestAssured, but you can choose any library to hit 
your endpoints.
We are testing that our service starts correctly and the health-check responds 
with a 200 status code.

Here is an example that uses `DockerUnitRunner`:
```java
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.jayway.restassured.RestAssured;

// Enables Dockerunit
@RunWith(DockerUnitRunner.class) 
public class MyServiceTest {

	@Test
  // Selects the previously defined descriptor
	@Use(service=MyServiceDescriptor.class) 
	public void healthCheckShouldReturn200(ServiceContext context) {
		// Gets the service based on value in the @Named annotation
		Service s = context.getService("my-spring-service"); 
		// Selects an available instance (you could declare more than one)
		ServiceInstance si = s.getInstances().stream().findAny().get(); 
		
		RestAssured
			.given()
				/* Uses the ip and port of the instance. 
				The port could be dynamic if @PublishPorts is used */
				.baseUri("http://" + si.getIp() + ":" + si.getPort()) 
			.when()
        // Hits the health-check endpoint
				.get("/health-check")  
			.then()
				.assertThat()
				.statusCode(200);
	}
```

Here is an example that uses `DockerUnitRule`:
```java
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;

import com.github.qzagarese.dockerunit.DockerUnitRule;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.jayway.restassured.RestAssured;

// Selects the previously defined descriptor
@Use(service=MyServiceDescriptor.class) 
public class MyServiceTest {

	@Rule
	public DockerUnitRule rule = new DockerUnitRule();

	private ServiceContext context;
    
	@Before
	public void setup() {
		context = DockerUnitRule.getDefaultServiceContext();
	}

	@Test
	public void healthCheckShouldReturn200() {
		// Gets the service based on value in the @Named annotation
		Service s = context.getService("my-spring-service"); 
		// Selects an available instance (you could declare more than one)
		ServiceInstance si = s.getInstances().stream().findAny().get(); 
		
		RestAssured
			.given()
				/* Uses the ip and port of the instance. 
				The port could be dynamic if @PublishPorts is used */
				.baseUri("http://" + si.getIp() + ":" + si.getPort()) 
			.when()
        // Hits the health-check endpoint
				.get("/health-check") 
			.then()
				.assertThat()
				.statusCode(200);
	}
```

This is Dockerunit in a nutshell.
1. It uses your descriptors to instantiate one or more Docker containers.
2. It makes sure that each of them started successfully and that the discovery 
provider (for now Consul) can monitor their state.
3. It provides you a ServiceContext instance that you can use to select the 
services and endpoints to hit.
4. It cleans up the containers after the the test execution (also when the 
test fails unexpectedly).
  
What next? You can look at some examples [here](./examples/).
  
