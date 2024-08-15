package com.itexchange.labs.discovery;

import com.itexchange.labs.loadbalancer.LoadBalancer;

import java.util.List;

public interface ServiceDiscovery {
    void registerService(String serviceName, String serviceInstance);

    void deregisterService(String serviceName, String serviceInstance);

    String discoverService(String serviceName);

    List<String> getAllInstances(String serviceName);

    void setLoadBalancer(LoadBalancer loadBalancer);

    LoadBalancer getLoadBalancer();
}
