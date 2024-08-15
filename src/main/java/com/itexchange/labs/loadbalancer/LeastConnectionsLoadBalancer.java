package com.itexchange.labs.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeastConnectionsLoadBalancer implements LoadBalancer {
    private final Map<String, Integer> connectionsCounts = new ConcurrentHashMap<>();

    @Override
    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) {
            return null;
        }

        return instances.stream()
                .min((a, b) -> Integer.compare(getConnectionCount(a), getConnectionCount(b)))
                .orElse(instances.getFirst());
    }

    private int getConnectionCount(String instance) {
        return connectionsCounts.getOrDefault(instance, 0);
    }

    public void incrementConnection(String instance) {
        connectionsCounts.merge(instance, 1, Integer::sum);
    }

    public void decrementConnection(String instance) {
        connectionsCounts
                .merge(instance, -1,
                        (oldValue, value) -> oldValue + value == 0 ? null : oldValue + value
                );
    }
}
