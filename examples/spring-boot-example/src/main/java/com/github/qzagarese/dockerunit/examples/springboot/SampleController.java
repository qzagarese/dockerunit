package com.github.qzagarese.dockerunit.examples.springboot;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
	
	@Autowired
	private Properties props;
	
	@GetMapping("/health-check")
	public ResponseEntity<String> healthcheck() {
		return ResponseEntity.ok("OK");
	}
	
	@GetMapping(value = "/greeting", produces = {"application/json"})
	public ResponseEntity<String> greeting() {
		String json = "{"
				+ "\"greeting\" : \"" + props.getProperty("greeting") + "\""
				+ "}";
		return ResponseEntity.ok(json);
	}
	
	@GetMapping(value = "/env/foo", produces = {"application/json"})
	public ResponseEntity<String> foo() {
		return getEnv("FOO");
	}
	
	@GetMapping(value = "/env/bar", produces = {"application/json"})
	public ResponseEntity<String> bar() {
		return getEnv("BAR");
	}

	private ResponseEntity<String> getEnv(String envVar) {
		String json = "{"
				+ "\"value\" : \"" + System.getenv(envVar) + "\""
				+ "}";
		return ResponseEntity.ok(json);
	}
	
}
