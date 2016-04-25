Regional Internet Registry Service
==============

This is a microservice simulating Regional Internet Registry (RIR).

The service is implemented in Java using Spring Boot.


Technologies
------------

- Spring Boot (REST support and embedded Tomcat server).
- JPA.
- Spring Actuator (monitoring and health check).
- H2 database.
- JUnit.
- Maven


How To Run
----------

Build: mvn clean install

Run: java -jar target/rir-service-1.0-SNAPSHOT.jar


Usage
-----

- http://localhost:8888/isp/list - list of all ISPs.
- http://localhost:8888/isp/{id} - ISP with the specified id.
- http://localhost:8888/isp?companyName={companyName} - list of ISPs with the specified companyName.
- http://localhost:8888/isp?website={website} - list of ISPs with the specified website.
- http://localhost:8888/isp?email={email} - ISP with the specified email.
- http://localhost:8888/isp/search?companyName={searchTerm} - list of ISPs which companyName contains the specified searchTerm.
- http://localhost:8888/isp/search?website={searchTerm} - list of ISPs which website contains the specified searchTerm.
- http://localhost:8888/isp/search?email={searchTerm} - list of ISPs which email contains the specified searchTerm.
- http://localhost:8888/isp/register - registering new ISPs (need to provide POST request with the data convertible to ISP POJO).
- http://localhost:8889/health - the service health check.
- http://localhost:8889/mappings - list of available HTTP endpoints.
- http://localhost:8889/metrics - some metrics for the service.

Assumtion: ISP is described by companyName, website and email and assumption is made that ISPs may have same names and websites but emails are unique!

RIR Service supports both JSON and XML formats: it provides the result for GET requests in different formats depending on "Accept" request header. User "application/json" to get JSON response and "application/xml" to get XML response.

Errors are described with different HTTP response codes:

- if the requested data was not found the response code is 404 (Not Found).
- if there was an error during new ISP registration (duplicate email for example) the response code is 422 (Unprocessable Entity).
- if incorrect URL was invoked the response code is 400 (Bad Request) or  404 (Not Found).


Configuration
-------------

There are two configuration files in src/main/resources directory:

- application.properties - allows to set the main service port,m anagement endpoints port and he address that the management endpoints are available on.
- logback.xml - logging configuration providing both console and file logging by default. File logs are created in logs directory.