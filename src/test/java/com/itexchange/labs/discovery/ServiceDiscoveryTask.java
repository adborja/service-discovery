package com.itexchange.labs.discovery;

import java.util.concurrent.Callable;

public class ServiceDiscoveryTask implements Callable<String> {
    private final ServiceDiscovery serviceDiscovery;
    private final String serviceName;


    public ServiceDiscoveryTask(ServiceDiscovery serviceDiscovery, String serviceName) {
        this.serviceDiscovery = serviceDiscovery;
        this.serviceName = serviceName;
    }

    @Override
    public String call() {
        var instance = serviceDiscovery.discoverService(serviceName);
        System.out.println(Thread.currentThread().getName() + " got instance [" + instance + "] using " + serviceDiscovery.getLoadBalancer());
        return instance;
    }
}
