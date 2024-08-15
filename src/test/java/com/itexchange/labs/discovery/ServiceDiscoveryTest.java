package com.itexchange.labs.discovery;

import com.itexchange.labs.loadbalancer.LeastConnectionsLoadBalancer;
import com.itexchange.labs.loadbalancer.RandomLoadBalancer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Every;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ServiceDiscoveryTest {
    private ServiceDiscovery serviceDiscovery;

    private static final String ACCOUNT_SERVICE = "account-service";
    private static final String USER_SERVICE = "user-service";

    private static final String INSTANCE_1 = "instance-1";
    private static final String INSTANCE_2 = "instance-2";
    private static final String INSTANCE_3 = "instance-3";

    @BeforeEach
    void setup() {
        var pairs = Arrays.asList(
                new ImmutablePair<>(ACCOUNT_SERVICE, INSTANCE_1),
                new ImmutablePair<>(ACCOUNT_SERVICE, INSTANCE_2),
                new ImmutablePair<>(ACCOUNT_SERVICE, INSTANCE_3),
                new ImmutablePair<>(USER_SERVICE, INSTANCE_1),
                new ImmutablePair<>(USER_SERVICE, INSTANCE_2),
                new ImmutablePair<>(USER_SERVICE, INSTANCE_3)
        );

        serviceDiscovery = new ServiceDiscoveryImpl();
        pairs.forEach(p -> serviceDiscovery.registerService(p.getLeft(), p.getRight()));
    }

    @Test
    void should_discover_instance_using_random_load_balancer_and_fixed_thread_pool() {
        final int numberOfThreads = 1000;

        try (var executor = Executors.newFixedThreadPool(numberOfThreads)) {
            var instances = Stream.generate(() -> new ServiceDiscoveryTask(serviceDiscovery, ACCOUNT_SERVICE))
                    .limit(numberOfThreads)
                    .map(executor::submit)
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
            MatcherAssert.assertThat(instances, Matchers.hasSize(numberOfThreads));
        }
    }

    @Test
    void should_discover_instance_using_random_load_balancer_and_virtual_threads() {
        serviceDiscovery.setLoadBalancer(new RandomLoadBalancer());

        final int numberOfThreads = 1000;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var instances = Stream.generate(() -> new ServiceDiscoveryTask(serviceDiscovery, USER_SERVICE))
                    .limit(numberOfThreads)
                    .map(executor::submit)
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();

            MatcherAssert.assertThat(instances, Matchers.hasSize(numberOfThreads));
        }
    }

    @Test
    void should_discover_instance_using_least_connections_load_balancer_and_fixed_thread_pool() {
        var leastConnectionsLoadBalancer = new LeastConnectionsLoadBalancer();
        serviceDiscovery.setLoadBalancer(leastConnectionsLoadBalancer);

        IntStream.range(0, 5).forEach(i -> leastConnectionsLoadBalancer.incrementConnection(INSTANCE_1));
        IntStream.range(0, 5).forEach(i -> leastConnectionsLoadBalancer.incrementConnection(INSTANCE_2));

        final int numberOfThreads = 10;

        try (var executor = Executors.newFixedThreadPool(numberOfThreads)) {
            var instances = Stream.generate(() -> new ServiceDiscoveryTask(serviceDiscovery, ACCOUNT_SERVICE))
                    .limit(numberOfThreads)
                    .map(executor::submit)
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
            MatcherAssert.assertThat(instances, Matchers.hasSize(numberOfThreads));
            MatcherAssert.assertThat(instances, Every.everyItem(CoreMatchers.is(INSTANCE_3)));
        }
    }
}
