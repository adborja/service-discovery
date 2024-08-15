package com.itexchange.labs.loadbalancer;

import java.util.List;

public interface LoadBalancer {
    String selectInstance(List<String> instances);
}
