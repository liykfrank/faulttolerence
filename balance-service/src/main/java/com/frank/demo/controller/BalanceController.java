package com.frank.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.frank.demo.entity.Agency;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
public class BalanceController {
  private static final Logger LOGGER = LoggerFactory.getLogger(BalanceController.class);
  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @HystrixCommand(fallbackMethod = "findByIdFallback")
  @GetMapping("/agency/{id}")
  public Agency findById(@PathVariable Long id) {
    return this.restTemplate.getForObject("http://agency-service/" + id, Agency.class);
  }
  
  public Agency findByIdFallback(Long id) {
    Agency agency = new Agency();
    agency.setId(-1L);
    agency.setName("Can not connect to agency-service");
    return agency;
  }

  @GetMapping("/log-user-instance")
  public void logUserInstance() {
    ServiceInstance serviceInstance = this.loadBalancerClient.choose("microservice-provider-user");
    // 打印当前选择的是哪个节点
    BalanceController.LOGGER.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
  }
}
