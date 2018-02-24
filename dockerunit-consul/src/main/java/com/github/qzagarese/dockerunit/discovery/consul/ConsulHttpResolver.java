package com.github.qzagarese.dockerunit.discovery.consul;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;

public class ConsulHttpResolver {

	private final Vertx vertx = Vertx.vertx();
	private final HttpClient httpClient;
	private final String host;
	private final int port;
	private final ObjectMapper mapper = new ObjectMapper();
	
	public ConsulHttpResolver(String host, int port) {
		this.host = host;
		this.port = port;
		httpClient = HttpClientBuilder.create().build();
	}

	public Void verifyCleanup(String serviceName, int expectedRecords, int timeoutInSeconds, int frequencyInSeconds) {
		BiConsumer<CompletableFuture<Void>, Throwable> errorConsumer = (fut, t) -> {
				fut.complete(null);
		};
		BiConsumer<CompletableFuture<Void>, List<ServiceRecord>> matchingConsumer = (fut, records) -> fut.complete(null);
		return performQuerying(serviceName, expectedRecords, timeoutInSeconds, frequencyInSeconds, errorConsumer,
				matchingConsumer);
	}

	public List<ServiceRecord> resolveService(String serviceName, int expectedRecords, int timeoutInSeconds,
			int frequencyInSeconds) {
		BiConsumer<CompletableFuture<List<ServiceRecord>>, Throwable> errorConsumer = (fut, t) -> {
		};
		BiConsumer<CompletableFuture<List<ServiceRecord>>, List<ServiceRecord>> matchingConsumer = (fut, records) -> fut
				.complete(records);
		return performQuerying(serviceName, expectedRecords, timeoutInSeconds, frequencyInSeconds, errorConsumer,
				matchingConsumer);
	}

	private <T> T performQuerying(String serviceName, int expectedRecords, int timeoutInSeconds, int frequencyInSeconds,
			BiConsumer<CompletableFuture<T>, Throwable> errorConsumer,
			BiConsumer<CompletableFuture<T>, List<ServiceRecord>> matchingConsumer) {
		CompletableFuture<T> result = new CompletableFuture<>();
		final AtomicInteger counter = new AtomicInteger(0);
		vertx.setPeriodic(frequencyInSeconds * 1000, timerId -> {
				HttpGet get = new HttpGet("http://" + host + ":" + port + "/v1/catalog/service/" + serviceName);
				HttpResponse response = null;
				try {
					response = httpClient.execute(get);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int counterValue = counter.incrementAndGet();
				List<ServiceRecord> records = getRecords(response);
				if(records !=  null && records.size() == expectedRecords) {
					vertx.cancelTimer(timerId);
					matchingConsumer.accept(result, records);
				} else {
					if (timedout(timeoutInSeconds, frequencyInSeconds, counterValue)) {
						vertx.cancelTimer(timerId);
						result.completeExceptionally(new RuntimeException("Discovery timed out."));
					}
				}
			});

		result.exceptionally(ex -> {
			throw new RuntimeException("Discovery/cleanup failed for service " + serviceName);
		});
		return result.join();
	}

	private List<ServiceRecord> getRecords(HttpResponse response) {
		if (response.getStatusLine().getStatusCode() != 200) {
			return null;
		}
		List<ServiceRecord> records = null;

		try {
			records = mapper.reader().forType(new TypeReference<List<ServiceRecord>>() {
			}).readValue(response.getEntity().getContent());
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}
	
	private boolean timedout(int timeoutInSeconds, int frequencyInSeconds, int counterValue) {
		return counterValue * frequencyInSeconds >= timeoutInSeconds;
	}
}
