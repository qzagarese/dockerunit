package com.github.qzagarese.dockerunit.examples.springboot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class AppRunner {

	
	public static void main(String[] args) {
		SpringApplication.run(AppRunner.class, args);
	}
	
	@Bean
	@Primary
	public Properties loadProperties() throws FileNotFoundException, IOException {
		String propsLocation = System.getProperty("properties.location");
		Properties props = new Properties();
		props.load(new FileReader(new File(propsLocation)));
		return props;
	}
	
}
