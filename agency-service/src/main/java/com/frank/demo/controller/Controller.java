package com.frank.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.frank.demo.entity.Agency;
import com.frank.demo.repository.AgencyRepository;

@RestController
public class Controller {
  @Autowired
  private AgencyRepository agencyRepository;

  @GetMapping("/{id}")
  public Agency findById(@PathVariable Long id) {
    Agency findOne = this.agencyRepository.findOne(id);
    return findOne;
  }
}
