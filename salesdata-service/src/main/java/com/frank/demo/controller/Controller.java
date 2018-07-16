package com.frank.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.frank.demo.entity.SalesData;
import com.frank.demo.repository.SalesDataRepository;

@RestController
public class Controller {
  @Autowired
  private SalesDataRepository userRepository;

  @GetMapping("/{id}")
  public SalesData findById(@PathVariable Long id) {
    SalesData findOne = this.userRepository.findOne(id);
    return findOne;
  }
}
