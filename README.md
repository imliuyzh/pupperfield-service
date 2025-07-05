# Pupperfield Backend

This is a mock API service based off the Fetch Rewards' front end take-home interview question.
The original problem statement is available at: https://frontend-take-home.fetch.com.

Other than that, you can also check out its client application, https://github.com/imliuyzh/pupperfield,
which is implemented by myself too.

## Getting Started

Before following the instructions below, please make sure you have Java v21 installed. If you
also want to run the `populator` program, make sure you have read the README file in `/populator`.

### Running the Application

The application will start on port 8080 by default:

```
`./mvnw spring-boot:run -Dspring-boot.run.profiles=localhost`
```

API documentation is available at: http://localhost:8080/swagger-ui.html.

### Testing the Application

TODO

## Technology Stack

This is a primarily a Java application with its database built by a Go program.

| Language | Framework/Library                                                                                                                                                          |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java     | Apache Commons Lang <br> Caffeine <br> JJWT <br> Logbook <br> Lombok <br> MapStruct <br> Spring Boot <br> Spring Cache <br> Spring Data JPA <br> Spring MVC <br> SpringDoc |
| Go       | modernc.org/sqlite                                                                                                                                                             |

## API Comparison

- `POST /auth/login` — Log in with email and name, receive JWT cookie
  - A few cookie attributes and header/value pairs are different due to the inner workings of Spring.

- `GET /dogs/search`
  - TODO

- `POST /dogs`
  - Fields are sorted by their names in alphabetical order.

- `GET /status`
    - A new health check endpoint that is missing from the original.

Validation errors are returned in a different format (see API documentation) with HTTP 422.
