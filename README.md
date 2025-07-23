# Pupperfield Service

This is a port of Fetch Rewards' front end take-home interview question.
The original problem statement is available at: https://frontend-take-home.fetch.com.

You can check out a client application, https://github.com/imliuyzh/pupperfield,
which uses the same API.

## Getting Started

Before following the instructions below, please make sure you have Java v21 installed. If you want
to run the `populator` program, make sure you also read the README file in `/populator`.

It is highly recommended to develop this project on Linux with IntelliJ IDEA. 

### Running the Application

Before starting the application, you need to set up the dependencies for the application:

```
./mvnw clean install
```

Then, run the application with the following command:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=localhost
```

The service will start on port 8080 by default. API documentation is available at: http://localhost:8080/swagger-ui.html.

### Testing the Application

You can see the execution status as well as getting the full coverage report at `/target/site/jacoco/index.html` with:

```
./mvnw clean test
```

## Technology Stack

This is a primarily a Java application with its database built by a Go program.

| Language | Library                                                                                                                                                                                                               |
|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java     | Apache Commons Lang <br> Caffeine <br> HikariCP <br> JJWT <br> JaCoCo <br> Logbook <br> Lombok <br> MapStruct <br> SQLite JDBC <br> Spring Boot <br> Spring Cache <br> Spring Data JPA <br> Spring MVC <br> SpringDoc |
| Go       | modernc.org/sqlite                                                                                                                                                                                                    |

## API Comparison

1. Stricter validation constraints are placed upon the user input. 
2. HTTP 401 and HTTP 422 responses include more error information.
3. HTTP 405 is used for unsupported HTTP methods instead of HTTP 404.
4. HTTP 415 is used for unsupported media types instead of returning the response in a different format.
5. A white label error page is returned when the URL includes invalid characters.

6. `POST /auth/login`
   - Different cookie attributes and header/value pairs are sent due to Spring internal implementation.

7. `GET /dogs/search`
   - Maximum number of dogs can include as much as the total number of rows in database.
   - "prev" and "next" fields are not included in the response when the "from" parameter value returned with those links is out of bounds.
     - For "prev," it is included when "from" is still equal or greater than 0.
     - For "next," it is included when "from" is greater than zero and less than the total number of result.
   - Parameter order in "next" and "prev" fields may differ from the original sometimes.
   - Dog IDs can be listed differently due to `populator`'s processing order.
   - `,` can be used to pass in multiple values for a query string parameter.
     - Each parameter value must be listed out one by one in the original implementation. 
   - HTTP 422 instead of HTTP 400/500 is used when an out of range value is provided for some query string parameters.
   - HTTP 422 instead of HTTP 400 is used when some query string parameters are repeated.

8. `POST /dogs`
   - Fields are sorted by their names in alphabetical order.
   - A nonexistent dog ID is omitted from the response.
   - Duplicated dog information is listed only once.

9. `GET /status`
   - A new health check endpoint that is missing from the original.
