# faulttolerence
//    docker run -d -it --name rabbit --hostname rabbit -p 30000:5672 -p 30001:15672 rabbitmq:management
//
//      998  docker run -d -it --name es -p 9200:9200 -p 9300:9300 elasticsearch
//  999  docker run -d -it --name kibana --link es:elasticsearch -p 5601:5601 kibana

//docker inspect --format '{{ .NetworkSettings.IPAddress }}' 8570b38e5877
 //  docker inspect --format '{{ .NetworkSettings.IPAddress }}'  36ba0255c836
//    docker run -d -it --name logstash logstash -e 'input { rabbitmq { host => "172.17.0.2" port => 5672 queue => "helloq"  durable => true } }  output { elasticsearch { hosts => ["172.17.0.3"] } }'
*For other versions of OpenShift, follow the instructions in the corresponding branch e.g. ocp-3.9, ocp-3.7, etc

# Fault tolerence - Hystrix and Turbine with RabbitMQ 

Hystrix integration with Turbine via RabbitMQ

## Sections

* [Introduction](#introduction)
* [Prerequisites](#prerequisites)

* [Agency Service](#agency-service)
* [Salesdata Service](#salesdata-service)
* [Balance Service](#balance-service)
* [Demo Guide](#demo-guide)
* [Demo Guide](#demo-guide)
* [Demo Guide](#demo-guide)



## Introduction

This project provides an implementation of circuit breaker with hystrix. Further more it will leverage turbine to gather the distributted hystrix stream informations. This allows you to monitor all of the hystrix status easily. Each application instance pushes the metrics from Hystrix commands to Turbine through a central RabbitMQ broker.

The demo includes 6 applications, 4 fucntion services and 2 infrastructure services:

![](images/comps.png?raw=true)

Below diagram explains the dependencies and communications of above services  

![](images/hystrix.png?raw=true)

The following diagram shows the communication model of Spring Cloud Turbine AMQP:

![](images/turbineamqp.png?raw=true)

The application used in this pipeline is a JAX-RS application which is available on GitHub and is imported into Gogs during the setup process:
[https://github.com/OpenShiftDemos/openshift-tasks](https://github.com/OpenShiftDemos/openshift-tasks/tree/eap-7)

## Prerequisites
* As RabbitMQ is used as the broker of histrix metrics collection. you need to install RabbitMQ first. here I use docker to create a RabbitMQ instance with management plugin.

~~~~

  docker run -d -it --name rabbit --hostname rabbit -p 30000:5672 -p 30001:15672 rabbitmq:management

~~~~
  
  

* The web console could be accessed with http://localhost:30001 with guest/guest

## Agency Service

This service is nothing more than an ordinary spring boot application:
For the data persistence a in-memory H2 database was used.     
The following is the dependencies used in this project:

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
```

To enable loading of the `DiscoveryClient`, add `@EnableDiscoveryClient` to the according configuration or application class like this:

```java
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
```
Here is the configuration in `application
.properties`:

```
server:
  port: 8091
spring:
  application:
    name: agency-service
  jpa:
    generate-ddl: false
    show-sql: true
    hibernate:
      ddl-auto: none
  datasource:                           
    platform: h2                       
    schema: classpath:schema.sql       
    data: classpath:data.sql
```
You could build and run this application follow below  steps:
- Go to agency-service directory
```
cd agency-service
```
- Compile with maven
```
mvn clean package
```
- Run this service
```
java -jar target/agency-service-0.0.1-SNAPSHOT.jar
```

- Then you could test this service with below link:
http://localhost:8091/1

And you will get something like this:

![](images/agency.png?raw=true)

## Salesdata Service

This service is nothing more than an ordinary spring boot application:
For the data persistence a in-memory H2 database was used.     
The following is the dependencies used in this project:

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
```

To enable loading of the `DiscoveryClient`, add `@EnableDiscoveryClient` to the according configuration or application class like this:

```java
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
```
Here is the configuration in `application
.properties`:

```
server:
  port: 8092
spring:
  application:
    name: salesdata-service
  jpa:
    generate-ddl: false
    show-sql: true
    hibernate:
      ddl-auto: none
  datasource:                           
    platform: h2                        
    schema: classpath:schema.sql        
    data: classpath:data.sql
```
You could build and run this application follow below  steps:
- Go to agency-service directory
```
cd agency-service
```
- Compile with maven
```
mvn clean package
```
- Run this service
```
java -jar target/salesdata-service-0.0.1-SNAPSHOT.jar
```

- Then you could test this service with below link:
http://localhost:8091/1

And you will get something like this:

![](images/salesdata.png?raw=true)

## Balance Service

This service is nothing more than an ordinary spring boot application:
For the data persistence a in-memory H2 database was used.     
The following is the dependencies used in this project:

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
```

To enable loading of the `DiscoveryClient`, add `@EnableDiscoveryClient` to the according configuration or application class like this:

```java
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
```
Here is the configuration in `application
.properties`:

```
server:
  port: 8091
spring:
  application:
    name: agency-service
  jpa:
    generate-ddl: false
    show-sql: true
    hibernate:
      ddl-auto: none
  datasource:                           
    platform: h2                       
    schema: classpath:schema.sql       
    data: classpath:data.sql
```
You could build and run this application follow below  steps:
- Go to agency-service directory
```
cd agency-service
```
- Compile with maven
```
mvn clean package
```
- Run this service
```
java -jar target/agency-service-0.0.1-SNAPSHOT.jar
```

- Then you could test this service with below link:
http://localhost:8091/1

And you will get something like this:

![](images/agency.png?raw=true)