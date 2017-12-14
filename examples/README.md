# Dockerunit examples

This page describes some handy examples that cover most of the common configs one might want to set on a Docker container.

### Mounting a volume
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@Volume(host="/home/myuser/application.properties", container="/application.properties")
public class MyDescriptor {}
``` 
Useful but not quite enough for a test. Dockerunit supports mounting of resources from the test classpath.

```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@Volume(host="application.properties", container="/application.properties", useClasspath=true)
public class MyDescriptor {}
``` 
The snippet above mounts the `application.properties` file from the `src/test/resources` directory inside your project.

This way you can easily mount different config files for different tests inside your Docker container.

### Passing an environment variable
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@Env({"FOO=foo", "BAR=bar"})
public class MyDescriptor {}
``` 
This is equivalent to `docker run -e FOO=foo -e BAR=bar my-docker-image`

### Exposing a container port to the host
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@PortBinding(exposedPort=8080, hostPort=9080) 
public class MyDescriptor {}
``` 
This is equivalent to `docker run -p 9080:8080 my-docker-image`

### Exposing a container port to a random host port (to avoid port conflicts)
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@PublishPorts
public class MyDescriptor {}
``` 
This is equivalent to `docker run -P my-docker-image`

### Provide or override the command to execute once the container starts
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@Command({"cat", "/etc/hosts"})
public class MyDescriptor {}
``` 
This is equivalent to `docker run my-docker-image cat /etc/hosts`

### Running multiple instances of a container to test Kubernetes like scenarios
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@PublishPorts
public class MyDescriptor {}

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.annotation.Use;

@RunWith(DockerUnitRunner.class)
public class MyTestClass {

	@Test
	@Use(MyDescriptor.class, replicas=3)	
	public void myTest(ServiceContext ctx) {
	}

}

``` 
It is necessary to use `@PublishPorts` as `@PortBinding` would lead to multiple containers trying to bind the same port number.
Dynamic port numbers are allocated and can be retrieved from the `ServiceContext`.

### Ensuring that service A is up and running before B is started
```java
import com.github.qzagarese.dockerunit.DockerUnitRunner;
import com.github.qzagarese.dockerunit.ServiceContext;
import com.github.qzagarese.dockerunit.annotation.Use;

@RunWith(DockerUnitRunner.class)
public class MyTestClass {

	@Test
	@Use(ServiceADescriptor.class, order=1)
	@Use(ServiceBDescriptor.class, order=2)	
	public void myTest(ServiceContext ctx) {
	}

}

``` 
Dockerunit starts service A, performs discovery (make sure you expose a health-check endpoint and use `@WebHealthCheck` to configure it), then starts service B, performs discovery, finally it runs your test.

### This stuff is not enough. I need to provide specific config for my container.
Right. There is no `@SilverBullet` annotation, but there is a `@ContainerBuilder` one that allows you to send commands straight to docker-java.
```java
import com.github.qzagarese.dockerunit.annotation.*;
import com.github.dockerjava.api.command.CreateContainerCmd;

@Named("my-service")
@Image("my-docker-image")
@PublishPorts
public class MyDescriptor {

	@ContainerBuilder
	public CreateContainerCmd build(CreateContainerCmd cmd) {
		return cmd.withDns("8.8.4.4");
	}

}

```
Now your container uses the Google dns.
`@ContainerBuilder` methods are executed after all the annotations on your descriptor class have been interpreted, so you can always override any container configuration.

### I don't want to copy and paste @ContainerBuilder methods in all my descriptors. This should be a component.
And indeed you can make it more generic.
All you need to do is creating an annotation and an interpreter.

Here is the annotation:
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionMarker(DnsExtensionInterpreter.class)
public @interface Dns {

	String[] value();
	
}
```
And here is the interpreter:
```java
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.qzagarese.dockerunit.internal.TestDescriptor;

public class DnsExtensionInterpreter implements ExtensionInterpreter<Dns> {

	@Override
	public CreateContainerCmd build(TestDescriptor td, 
		CreateContainerCmd cmd, 
		Dns annotation) {
		return cmd.withDns(annotation.value());
	}

}
```
And this is how you use it:
```java
import com.github.qzagarese.dockerunit.annotation.*;

@Named("my-service")
@Image("my-docker-image")
@PublishPorts
@Dns({"8.8.4.4"})
public class MyDescriptor {}
```
You can build useful extension with little code. Give a look at [WebHealthCheckExtensionInterpreter](https://github.com/qzagarese/dockerunit/blob/master/dockerunit-consul/src/main/java/com/github/qzagarese/dockerunit/discovery/consul/annotation/impl/WebHealthCheckExtensionInterpreter.java)
 to see how Dockerunit registers your health check config in Consul.
 
Finally, you can give a look at the spring-boot-example to see some tests based on Dockerunit + RestAssured.