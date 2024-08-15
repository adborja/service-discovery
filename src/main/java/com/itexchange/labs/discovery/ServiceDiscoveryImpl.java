package com.itexchange.labs.discovery;

import com.itexchange.labs.loadbalancer.LoadBalancer;
import com.itexchange.labs.loadbalancer.RoundRobinLoadBalancer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceDiscoveryImpl implements ServiceDiscovery {
    private LoadBalancer loadBalancer;
    private final Map<String, List<String>> services = new ConcurrentHashMap<>();

    public ServiceDiscoveryImpl() {
        // default load balancer
        this.loadBalancer = new RoundRobinLoadBalancer();
    }

    @Override
    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public ServiceDiscoveryImpl(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void registerService(final String serviceName, final String serviceInstance) {
        services.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>())
                .add(serviceInstance);
    }

    @Override
    public void deregisterService(String serviceName, String serviceInstance) {
        Optional.ofNullable(services.get(serviceName))
                .ifPresent(instances -> {
                    instances.remove(serviceInstance);
                    if (instances.isEmpty()) {
                        services.remove(serviceName);
                    }
                });
    }

    @Override
    public String discoverService(String serviceName) {
        return Optional.ofNullable(services.get(serviceName))
                .filter(instances -> !instances.isEmpty())
                .map(loadBalancer::selectInstance)
                .orElseThrow(() -> new RuntimeException("no service instances available for " + serviceName));
    }

    @Override
    public List<String> getAllInstances(String serviceName) {
        return services.getOrDefault(serviceName, Collections.emptyList());
    }
}
