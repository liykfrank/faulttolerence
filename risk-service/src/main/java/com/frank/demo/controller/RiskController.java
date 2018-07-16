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

import com.frank.demo.entity.SalesData;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
public class RiskController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskController.class);
  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private LoadBalancerClient loadBalancerClient;

  @HystrixCommand(fallbackMethod = "findByIdFallback")
  @GetMapping("/sales/{id}")
  public SalesData findById(@PathVariable Long id) {
    return this.restTemplate.getForObject("http://salesdata-service/" + id, SalesData.class);
  }
  
  public SalesData findByIdFallback(Long id) {
	    SalesData SalesData = new SalesData();
	    SalesData.setId(-1L);
	    SalesData.setAgencyCode("Can not connect to microservice-provider-SalesData");
	    return SalesData;
	  }

  @GetMapping("/log-SalesData-instance")
  public void logSalesDataInstance() {
    ServiceInstance serviceInstance = this.loadBalancerClient.choose("microservice-provider-SalesData");
    // 打印当前选择的是哪个节点
    RiskController.LOGGER.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
  }
}
