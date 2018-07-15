package com.itmuch.cloud.study.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SalesData {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  @Column(name="agencyCode")
  private String agencyCode;

  @Column
  private BigDecimal amount;

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return this.agencyCode;
  }

  public void setUsername(String username) {
    this.agencyCode = username;
  }


  public BigDecimal getBalance() {
    return this.amount;
  }

  public void setBalance(BigDecimal balance) {
    this.amount = balance;
  }

}
