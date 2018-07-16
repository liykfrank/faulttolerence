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
* [Demo Guide](#demo-guide)
* [Demo Guide](#demo-guide)
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

1. A Jenkins pipeline is pre-configured which clones Tasks application source code from Gogs (running on OpenShift), builds, deploys and promotes the result through the deployment pipeline. In the CI/CD project, click on _Builds_ and then _Pipelines_ to see the list of defined pipelines.

    Click on _tasks-pipeline_ and _Configuration_ and explore the pipeline definition.

    You can also explore the pipeline job in Jenkins by clicking on the Jenkins route url, logging in with the OpenShift credentials and clicking on _tasks-pipeline_ and _Configure_.

    go to agency-service directory via cd agency-service.
    compile with maven
    mvn clean package

    Run this service, java -jar target/agency-service-0.0.1-SNAPSHOT.jar

    Then you could test this service with below link:
    http://localhost:8091/1

    And you will get something like this:


    
This is something that you get for free just by adding the following dependency inside your project:

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
@SpringBootApplication
@EnableDiscoveryClient
public class Application {  
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

Then you can inject the client in your code simply by:

```java
@Autowired
private DiscoveryClient discoveryClient;
```

If for any reason you need to disable the `DiscoveryClient` you can simply set the following property in `application
.properties`:

```
spring.cloud.kubernetes.discovery.enabled=false
```

[//]: # "TODO: make clearer with an example and details on how to align service and application name"

Some Spring Cloud components use the `DiscoveryClient` in order to obtain info about the local service instance. For 
this to work you need to align the service name with the `spring.application.name` property.

2. Run an instance of the pipeline by starting the _tasks-pipeline_ in OpenShift or Jenkins.

## Deploy on RHPDS

If you have access to RHPDS, provisioning of this demo is automated via the service catalog under **OpenShift Demos &rarr; OpenShift CI/CD for Monolith**. If you don't know what RHPDS is, read the instructions in the next section.

## Automated Deploy on OpenShift
You can se the `scripts/provision.sh` script provided to deploy the entire demo:

  ```
  ./provision.sh --help
  ./provision.sh deploy --deploy-che --ephemeral
  ./provision.sh delete 
  ```
  
## Manual Deploy on OpenShift
Follow these [instructions](docs/local-cluster.md) in order to create a local OpenShift cluster. Otherwise using your current OpenShift cluster, create the following projects for CI/CD components, Dev and Stage environments:

  ```shell
  # Create Projects
  oc new-project dev --display-name="Tasks - Dev"
  oc new-project stage --display-name="Tasks - Stage"
  oc new-project cicd --display-name="CI/CD"

  # Grant Jenkins Access to Projects
  oc policy add-role-to-user edit system:serviceaccount:cicd:jenkins -n dev
  oc policy add-role-to-user edit system:serviceaccount:cicd:jenkins -n stage
  ```  

And then deploy the demo:

  ```
  # Deploy Demo
  oc new-app -n cicd -f cicd-template.yaml

  # Deploy Demo woth Eclipse Che
  oc new-app -n cicd -f cicd-template.yaml --param=WITH_CHE=true
  ```

To use custom project names, change `cicd`, `dev` and `stage` in the above commands to
your own names and use the following to create the demo:

  ```shell
  oc new-app -n cicd -f cicd-template.yaml --param DEV_PROJECT=dev-project-name --param STAGE_PROJECT=stage-project-name
  ```


## Troubleshooting


* If gogs starting fails with ```error: need to run with root or gogs```, Modify dc/gogs to run docker with root user.
  ```
  oc create serviceaccount useroot
  oc adm policy add-scc-to-user anyuid -z useroot
  oc patch dc/gogs --patch '{"spec":{"template":{"spec":{"serviceAccountName": "useroot"}}}}'
  
  ```
* If gogs is running very slow ```lookup _avatars-sec._tcp.gmail.com on xx.xx.xx.xx: read udp 172.17.0.4:43838->192.168.1.10:53: i/o timeout```, Modify dc/gogs to disable AVATAR.
  ```
  [picture]
	DISABLE_GRAVATAR = true
	ENABLE_FEDERATED_AVATAR = false
  
  ```

* If pipeline execution fails with ```error: no match for "jboss-eap70-openshift"```, import the jboss imagestreams in OpenShift.
  ```
  oc create -f https://raw.githubusercontent.com/jboss-openshift/application-templates/ose-v1.4.12/eap/eap70-image-stream.json -n openshift
  ```
* If Maven fails with `/opt/rh/rh-maven33/root/usr/bin/mvn: line 9:   298 Killed` (e.g. during static analysis), you are running out of memory and need more memory for OpenShift.

## Demo Guide

1. A Jenkins pipeline is pre-configured which clones Tasks application source code from Gogs (running on OpenShift), builds, deploys and promotes the result through the deployment pipeline. In the CI/CD project, click on _Builds_ and then _Pipelines_ to see the list of defined pipelines.

    Click on _tasks-pipeline_ and _Configuration_ and explore the pipeline definition.

    You can also explore the pipeline job in Jenkins by clicking on the Jenkins route url, logging in with the OpenShift credentials and clicking on _tasks-pipeline_ and _Configure_.

2. Run an instance of the pipeline by starting the _tasks-pipeline_ in OpenShift or Jenkins.

3. During pipeline execution, verify a new Jenkins slave pod is created within _CI/CD_ project to execute the pipeline.

4. Pipelines pauses at _Deploy STAGE_ for approval in order to promote the build to the STAGE environment. Click on this step on the pipeline and then _Promote_.

5. After pipeline completion, demonstrate the following:
  * Explore the _snapshots_ repository in Nexus and verify _openshift-tasks_ is pushed to the repository
  * Explore SonarQube and show the metrics, stats, code coverage, etc
  * Explore _Tasks - Dev_ project in OpenShift console and verify the application is deployed in the DEV environment
  * Explore _Tasks - Stage_ project in OpenShift console and verify the application is deployed in the STAGE environment  

![](images/sonarqube-analysis.png?raw=true)

6. Clone and checkout the _eap-7_ branch of the _openshift-tasks_ git repository and using an IDE (e.g. JBoss Developer Studio), remove the ```@Ignore``` annotation from ```src/test/java/org/jboss/as/quickstarts/tasksrs/service/UserResourceTest.java``` test methods to enable the unit tests. Commit and push to the git repo.

7. Check out Jenkins, a pipeline instance is created and is being executed. The pipeline will fail during unit tests due to the enabled unit test.

8. Check out the failed unit and test ```src/test/java/org/jboss/as/quickstarts/tasksrs/service/UserResourceTest.java``` and run it in the IDE.

9. Fix the test by modifying ```src/main/java/org/jboss/as/quickstarts/tasksrs/service/UserResource.java``` and uncommenting the sort function in _getUsers_ method.

10. Run the unit test in the IDE. The unit test runs green. 

11. Commit and push the fix to the git repository and verify a pipeline instance is created in Jenkins and executes successfully.

![](images/openshift-pipeline.png?raw=true)


## Using Eclipse Che for Editing Code

If you deploy the demo template using `WITH_CHE=true` paramter, or the deploy script and use `--deploy-che` flag, then an [Eclipse Che](https://www.eclipse.org/che/) instances will be deployed within the CI/CD project which allows you to use the Eclipse Che web-based IDE for editing code in this demo.


Here is a step-by-step guide for editing and pushing the code to the Gogs repository (step 6) using Eclipse Che.

Click on Eclipse Che route url in the CI/CD project which takes you to the workspace administration page. Select the *Java* stack and click on the *Create* button to create a workspace for yourself.

![](images/che-create-workspace.png?raw=true)

Once the workspace is created, click on *Open* button to open your workspace in the Eclipse Che in the browser.

![](images/che-open-workspace.png?raw=true)

It might take a little while before your workspace is set up and ready to be used in your browser. Once it's ready, click on **Import Project...** in order to import the `openshift-tasks` Gogs repository into your workspace.

![](images/che-import-project.png?raw=true)

Enter the Gogs repository HTTPS url for `openshift-tasks` as the Git repository url with Git username and password in the 
url: <br/>
`http://gogs:gogs@[gogs-hostname]/gogs/openshift-tasks.git`

 You can find the repository url in Gogs web console. Make sure the check the **Branch** field and enter `eap-7` in order to clone the `eap-7` branch which is used in this demo. Click on **Import**

![](images/che-import-git.png?raw=true)

Change the project configuration to  **Maven** and then click **Save**

![](images/che-import-maven.png?raw=true)

Configure you name and email to be stamped on your Git commity by going to **Profile > Preferences > Git > Committer**.

![](images/che-configure-git-name.png?raw=true)

Follow the steps 6-10 in the above guide to edit the code in your workspace. 

![](images/che-edit-file.png?raw=true)

In order to run the unit tests within Eclipse Che, wait till all dependencies resolve first. To make sure they are resolved, run a Maven build using the commands palette icon or by clicking on **Run > Commands Palette > build**. 

Make sure you run the build again, after fixing the bug in the service class.

Run the unit tests in the IDE after you have corrected the issue by right clicking on the unit test class and then **Run Test > Run JUnit Test**

![](images/che-run-tests.png?raw=true)

![](images/che-junit-success.png?raw=true)


Click on **Git > Commit** to commit the changes to the `openshift-tasks` git repository. Make sure **Push commited changes to ...** is checked. Click on **Commit** button.

![](images/che-commit.png?raw=true)

As soon the changes are committed to the git repository, a new instances of pipeline gets triggers to test and deploy the 
code changes.
