# FedEx Aggregate Api Service

This is a Java HTTP Rest server written for an FedEx job assessment. You can use the typical Maven commands to build a
fat JAR, but you can also run the main class `AggregateApiApplication` directly. Java 22 is required.
See further below how to create a Docker image.

## Useful commands
When running your application locally on port 8080 you can use the following URL's out of the box. When
deployed on other machines you must adjust host and port accordingly.

### Health and other actuator info

http://localhost:8080/actuator/health 

To enable other actuator info you can read Spring Boot documentation for enabling the various other options.

### Api swagger (OpenApi 3.0)

http://localhost:8080/swagger-ui/index.html

For the specification:

http://localhost:8080/v3/api-docs (json)

http://localhost:8080/v3/api-docs.yaml (yaml)

## External runtime dependencies

The aggregate service connects to three different REST API's on the same endpoint. 
Get the Docker image here:

https://hub.docker.com/r/xyzassessment/backend-services

After getting the Docker image you can run it in your Docker environment on port 9090 as follows:
```
docker container run -p 9090:8080 xyzassessment/backend-services:latest
```
Now you can access (with example query's):

http://localhost:9090/shipments?q=109347263,123456891

http://localhost:9090/track?q=109347263,123456891

http://localhost:9090/pricing?q=NL,CN

## Docker image deliverable
Make sure Java 22 is on your path while executing the following in the 
Java project root directory (e.g. `<your directory>/fedex-aggregate-api-full`)
```
./mvnw clean package spring-boot:build-image
```
If it fails because it has errors while downloading support packages, try it again (assuming you have an
Internet connection).

Now you can run the docker image:
```
docker container run -p 8080:8080 docker.io/library/aggregate-api:latest
```
and the URL's mentioned at the top will work because we mapped port 8080 to 8080.

## Functionality

### AS-1: As FedEx, I want to be able to query all services in a single network call to optimise network traffic.

This is fully implemented. You can read the code to see it is solved with the Spring WebFlux/ WebClient framework.
The three network calls to the FedEx API are execute in parallel and use non-blocking IO for optimal resource usage and speed.

### AS-2: as FedEx, I want service calls to be throttled and bulked into consolidated requests per respective API to prevent services from being overloaded

This is fully implemented using DeferredResult (Asyn Servlet API) and request caches. We use a Jetty server
(Tomcat had some issues in the past with DeferredResult).
We are not using Netty with the full WebFlux Reactor HTTP handler because that interferes with DeferredResult.
But when calling the FedEx API services we still use the WebFlux/ WebClient non-blocking solution.

Note that the implemented solution is not ideal. Holding HTTP requests, although using minimal resources, 
always requires the TCP/ HTTP stack to keep the connection 'alive'. This is discouraged from a 
Reactive perspective. 
To work around a backend that is sensitive for overload we can think of other solutions. The first
solution that comes in mind is to introduce a datastore that will contain the required info. We can either
make sure that all updates are also put in the datastore or we can make a background scheduled service to
keep the datastore close to the latest situation. Our web clients can query the datastore (using a 
HTTP Rest API server in between) and will always have a quick response. With this solution we keep 
the HTTP channels in good shape.

### AS-3: as FedEx, I want service calls to be scheduled periodically even if the queue is not full to prevent overly-long response times

This is fully implemented. This user story should go together with AS-2. It is not acceptable to have web clients 
waiting indefinitely. This is implemented using the timeout option of DeferredResult.
See the previous paragraph on AS-2 for more information.
