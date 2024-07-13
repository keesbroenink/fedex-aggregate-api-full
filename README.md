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

http://localhost:8080/webjars/swagger-ui/index.html

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
Java project root directory (e.g. `/users/kees/ideaprojects/fedex-aggregate-api`)
```
./mvnw clean package spring-boot:build-image
```
If it fails because it cannot download support packages from Internet but you have a
good connection, try it again.

Now you can run the docker image:
```
docker container run -p 8080:8080 docker.io/library/aggregate-api:latest
```
and the URL's mentioned at the top will work because we mapped port 8080 to 8080.

## Functionality

### AS-1: As FedEx, I want to be able to query all services in a single network call to optimise network traffic.

This is fully implemented. You can read the code to see it is solved with the Spring Boot WebFlux framework.
The three network calls to the FedEx API are execute in parallel and use non-blocking IO for optimal resource usage and speed.
Note that the test code does not cover all scenario's yet.

### AS-2: as FedEx, I want service calls to be throttled and bulked into consolidated requests per respective API to prevent services from being overloaded

This is not implemented. It is unclear how to use WebFlux to hold on to a HTTP client request when the requested items
are less than 5. With DeferredResult I can see a solution but that would mean to rewrite the code for AS-1.
Note that holding HTTP requests, although using minimal resources, always requires the TCP/ HTTP stack to
keep the connection alive. This is discouraged from a Reactive perspective. It would not sit well with the
approach taken with AS-1 where optimal resource usage was a key feature of the solution.
To working around a backend that is sensitive for overload we can think of different solutions. The first
solution that comes in mind is to introduce a datastore that will contain the required info. We could either
make sure that all updates are also put in the datastore or we could make a background scheduled service to
keep the datastore close to the latest situation. Our web clients can query the datastore and will always have a quick response
and we keep the HTTP channels in good shape.

### AS-3: as FedEx, I want service calls to be scheduled periodically even if the queue is not full to prevent overly-long response times

This user story should go together with AS-2. It is not acceptable to have web clients waits indefinitely.
See the previous paragraph for more information on AS-2 which also applies to AS-3.
