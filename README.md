# Dockerunit - JUnit for Docker containers

Dockerunit is an extensible framework for testing of dockerised services and applications.
It is based on JUnit and it allows linking of Docker images to Java tests by means of Java annotations.
You can think of Dockerunit as of a docker-compose for Java tests.

Dockerunit leverages useful tools like [docker-java](https://github.com/docker-java/docker-java), [Consul](https://www.consul.io/) and [registrator](https://github.com/gliderlabs/registrator) to provide the following main features:
1. Automatic pull of images referencing a registry.
2. Service discovery based on Consul + registrator (alternative discovery providers can be plugged in).
3. Container port mapping.
4. Volume mapping allowing relative paths from test classpath, so you can easily mount test config files.
5. Support for multiple instances of a service, so you can test how your services would work on environments like [Kubernetes](https://kubernetes.io/).
6. A simple mechanism similar to the [Java Validation Framework](https://jcp.org/en/jsr/detail?id=303) for you to add your own annotations.
 
## Usage
You can enable Dockerunit by adding the following dependencies to you POM file.
```xml
<dependency>
  <groupId>com.github.qzagarese</groupId>
  <artifactId>dockerunit-core</artifactId>
  <version>1.0.0-M1</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.github.qzagarese</groupId>
  <artifactId>dockerunit-consul</artifactId>
  <version>1.0.0-M1</version>
  <scope>test</scope>
</dependency>
```

## How it works
Building tests with Dockerunit consists of two main steps:
1. Defining your service descriptor.
2. Using your service from your test.

### 1. Defining your service descriptor
A service descriptor is a class that instructs Dockerunit about how to create Docker containers, given a Docker image you have previously created.
Here is a simple descriptor for a Spring service listening on port 8080.

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
If not, Dockerunit cannot guarantee your service started successfully, 
before your test invokes its endpoints. */  
@WebHealthCheck(exposedPort=8080, endpoint="/health-check")
public class MyServiceDescriptor {
}
```

### 2. Using your service from your test
It's now time to write an actual test.
This example uses RestAssured, but you can choose any library to hit your endpoints.
We are testing that our service starts correctly and the health-check responds with a 200 status code.

```java
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.Service;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.ServiceInstance;
import com.github.qzagarese.dockerunit.annotation.Use;
import com.jayway.restassured.RestAssured;

@RunWith(DockerUnitRunner.class) // Enables Dockerunit
public class MyServiceTest {

	@Test
	@Use(service=MyServiceDescriptor.class) // Selects the previously defined descriptor
	public void healthCheckShouldReturn200(ServiceContext context) {
		Service s = context.getService("my-spring-service"); // Gets the service based on value in the @Named annotation
		ServiceInstance si = s.getInstances().stream().findAny().get(); // Selects the available instance (you could declare more than one)
		
		RestAssured
			.given()
				.baseUri("http://" + si.getIp() + ":" + si.getPort()) // Uses the ip and port of the instance. The port could be dynamic if @PublishPorts is used
			.when()
				.get("/health-check") // Hits the health-check endpoint 
			.then()
				.assertThat()
				.statusCode(200);
	}
```

This is Dockerunit in a nutshell.
1. It uses your descriptors to instantiate one or more Docker containers.
2. It makes sure that each of them started successfully and that the discovery provider (for now Consul) can monitor their state.
3. It provides you a ServiceContext instance that you can use to select the services and endpoints to hit.
4. It cleans up the containers after the the test execution (also when the test fails unexpectedly).
  
More examples are coming along with a complete Getting Started guide.  