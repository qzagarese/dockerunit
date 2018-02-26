package com.github.qzagarese.dockerunit.discovery.consul;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.qzagarese.dockerunit.discovery.consul.ServiceRecord.Check;

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
			List<ServiceRecord> records = null;
				try {
					records = getHealthyRecords(serviceName);
				} catch (Throwable t) {
					result.completeExceptionally(t);
				}
				int counterValue = counter.incrementAndGet();
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

	private List<ServiceRecord> getHealthyRecords(String serviceName) throws IOException, ClientProtocolException {
		List<ServiceRecord> allRecords = getCatalog(serviceName);
		List<ServiceRecord> unhealthy = getUnhealthy(serviceName);
		return allRecords.stream()
				.filter(r -> {
					return unhealthy.stream()
						.filter(uh -> uh.getPort() == r.getPort())
						.collect(Collectors.toList()).size() == 0;						
				}).collect(Collectors.toList());
	}

	private List<ServiceRecord> getUnhealthy(String serviceName) throws ClientProtocolException, IOException {
		List<ServiceRecord> records;
		HttpResponse response = null;
		HttpGet get = new HttpGet("http://" + host + ":" + port + "/v1/health/service/" + serviceName);
		response = httpClient.execute(get);
		records = parseUnhealthy(response);
		return records;
	}

	private List<ServiceRecord> getCatalog(String serviceName) throws ClientProtocolException, IOException {
		HttpResponse response = null;
		HttpGet get = new HttpGet("http://" + host + ":" + port + "/v1/catalog/service/" + serviceName);
		response = httpClient.execute(get);
		return mapper.reader().forType(new TypeReference<List<ServiceRecord>>() {
		}).readValue(response.getEntity().getContent());
	}

	private List<ServiceRecord> parseUnhealthy(HttpResponse response) throws UnsupportedOperationException, IOException {
		List<ServiceRecord> records = mapper.reader().forType(new TypeReference<List<ServiceRecord>>() {
		}).readValue(response.getEntity().getContent());
		if(records != null) {
			records = records.stream()
					.filter(r -> {
						List<Check> failingChecks = new ArrayList<>();
						if(r.getChecks() != null) {
							failingChecks = r.getChecks().stream()
								.filter(c -> !c.getStatus().equalsIgnoreCase(Check.PASSING))
								.collect(Collectors.toList());
						}
						return failingChecks.size() > 0;
					}).collect(Collectors.toList());
		}
		return records;
	}
	
	private boolean timedout(int timeoutInSeconds, int frequencyInSeconds, int counterValue) {
		return counterValue * frequencyInSeconds >= timeoutInSeconds;
	}
}
