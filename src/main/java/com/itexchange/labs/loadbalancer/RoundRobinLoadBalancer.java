package com.itexchange.labs.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    @Override
    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) {
            return null;
        }

        int index = currentIndex.getAndUpdate(i -> (i + 1) % instances.size());
        return instances.get(index);
    }
}
