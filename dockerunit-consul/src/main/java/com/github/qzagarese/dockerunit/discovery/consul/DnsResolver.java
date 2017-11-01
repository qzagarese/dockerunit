package com.github.qzagarese.dockerunit.discovery.consul;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import io.vertx.core.dns.DnsException;
import io.vertx.core.dns.DnsResponseCode;
import io.vertx.core.dns.SrvRecord;

public class DnsResolver {

	private final Vertx vertx = Vertx.vertx();
	private final DnsClient dnsClient;

	public DnsResolver(String host, int port) {
		dnsClient = vertx.createDnsClient(port, host);
	}

	public Void verifyCleanup(String serviceName, int expectedRecords, int timeoutInSeconds, int frequencyInSeconds) {
		BiConsumer<CompletableFuture<Void>, Throwable> errorConsumer = (fut, t) -> {
			if (t instanceof DnsException && ((DnsException) t).code().equals(DnsResponseCode.NXDOMAIN)) {
				fut.complete(null);
			}
		};
		BiConsumer<CompletableFuture<Void>, List<SrvRecord>> matchingConsumer = (fut, records) -> fut.complete(null);
		return performQuerying(serviceName, expectedRecords, timeoutInSeconds, frequencyInSeconds, errorConsumer,
				matchingConsumer);
	}

	public List<SrvRecord> resolveSRV(String serviceName, int expectedRecords, int timeoutInSeconds,
			int frequencyInSeconds) {
		BiConsumer<CompletableFuture<List<SrvRecord>>, Throwable> errorConsumer = (fut, t) -> {
		};
		BiConsumer<CompletableFuture<List<SrvRecord>>, List<SrvRecord>> matchingConsumer = (fut, records) -> fut
				.complete(records);
		return performQuerying(serviceName, expectedRecords, timeoutInSeconds, frequencyInSeconds, errorConsumer,
				matchingConsumer);
	}

	private <T> T performQuerying(String serviceName, int expectedRecords, int timeoutInSeconds, int frequencyInSeconds,
			BiConsumer<CompletableFuture<T>, Throwable> errorConsumer,
			BiConsumer<CompletableFuture<T>, List<SrvRecord>> matchingConsumer) {
		CompletableFuture<T> result = new CompletableFuture<>();
		final AtomicInteger counter = new AtomicInteger(0);
		vertx.setPeriodic(frequencyInSeconds * 1000, timerId -> {
			dnsClient.resolveSRV(serviceName, ar -> {
				int counterValue = counter.incrementAndGet();
				if (ar.succeeded()) {
					if (ar.result().size() == expectedRecords) {
						vertx.cancelTimer(timerId);
						matchingConsumer.accept(result, ar.result());
					} else {
						if (timedout(timeoutInSeconds, frequencyInSeconds, counterValue)) {
							vertx.cancelTimer(timerId);
							result.completeExceptionally(new RuntimeException("Discovery timed out."));
						}
					}
				} else {
					if (timedout(timeoutInSeconds, frequencyInSeconds, counterValue)) {
						vertx.cancelTimer(timerId);
						result.completeExceptionally(new RuntimeException("Discovery timed out", ar.cause()));
					} else {
						errorConsumer.accept(result, ar.cause());
					}
				}
			});

		});
		result.exceptionally(ex -> {
			throw new RuntimeException("Discovery/cleanup failed for service " + serviceName);
		});
		return result.join();
	}

	private boolean timedout(int timeoutInSeconds, int frequencyInSeconds, int counterValue) {
		return counterValue * frequencyInSeconds >= timeoutInSeconds;
	}
}
