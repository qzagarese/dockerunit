# Spring Boot Example

This is a simple project showing how easily you can integrate Dockerunit inside your dev/test cycle.
The example uses Spring Boot for the tested service, however we are speaking about container tests, so the same applies for any other framework and, if you have already built your container, for any language.

## Prerequisites
* Docker 17+ (it might work with previous versions of Docker, however this hasn't been tested yet).
* Java 8.

You don't need Maven as this example uses Maven wrapper.

## Building the example
To build the service jar file and the Docker image simply type the following:

`./mvnw clean package docker:build`

## Running the tests
Before running the tests, make sure you know the ip of the Docker bridge network interface.
This is usually `172.17.42.1`, however some distros and Docker for macOS might use a different ip.
To find out the ip of the Docker bridge interface, type the following:

`docker inspect bridge | grep Gateway`

If you are on Linux and the ip was `172.17.42.1` you can run the tests as follows:

`./mvnw test -P container-tests`

If you are on Linux but you have a different ip (for example `172.17.0.1`), run the following:

`./mvnw test -P container-tests -Ddocker.bridge.ip=172.17.0.1` 

If you are on Mac, run the following:

`./mvnw test -P container-tests -Ddocker.host=localhost -Ddocker.bridge.ip=172.17.0.1 -Dconsul.dns.enabled=false` 

   