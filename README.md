# Pupperfield Service

This is a port of the back end application used by Fetch Rewards' front end take-home
interview question. The original description is available at: https://frontend-take-home.fetch.com.

*You can check out a client application based on this API, https://github.com/imliuyzh/pupperfield,*
*which I implemented myself.*

## Getting Started

Before following the instructions below, please make sure you have Java v25+ installed. If you want
to run the `populator` program, make sure you also read the README file in `/populator`.

It is highly recommended to develop this project on Linux with IntelliJ IDEA. 

### Running the Application

Before starting the application, you need to set up the dependencies for the application:

```
./mvnw clean install
```

Then, run the following command:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=localhost
```

The service will start on port 8080 by default. API documentation is available at: http://localhost:8080/swagger-ui.html.

### Testing the Application

You can see the execution status as well as the coverage report at `/target/site/jacoco/index.html` with:

```
./mvnw clean test
```

## Technology Stack

This is a primarily a Java application with its database built by a Go program.

| Language | Library                                                                                                                                                                                                               |
|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java     | Apache Commons Lang <br> Caffeine <br> HikariCP <br> JJWT <br> JaCoCo <br> Logbook <br> Lombok <br> MapStruct <br> SQLite JDBC <br> Spring Boot <br> Spring Cache <br> Spring Data JPA <br> Spring MVC <br> SpringDoc |
| Go       | modernc.org/sqlite                                                                                                                                                                                                    |
