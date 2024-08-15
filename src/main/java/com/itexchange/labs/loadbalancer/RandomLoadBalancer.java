package com.itexchange.labs.loadbalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public String selectInstance(List<String> instances) {
        if (instances.isEmpty()) {
            return null;
        }

        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }
}
