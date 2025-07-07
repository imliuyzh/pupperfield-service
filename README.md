# Pupperfield Backend

This is a mock API service based off the Fetch Rewards' front end take-home interview question.
The original problem statement is available at: https://frontend-take-home.fetch.com.

Other than that, you can also check out its client application, https://github.com/imliuyzh/pupperfield,
which is implemented by myself too.

## Getting Started

Before following the instructions below, please make sure you have Java v21 installed. If you
also want to run the `populator` program, make sure you read the README file in `/populator`.

It is highly recommended to run this project on Linux. 

### Running the Application

The application will start on port 8080 by default:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=localhost
```

API documentation is available at: http://localhost:8080/swagger-ui.html.

### Testing the Application

TODO

## Technology Stack

This is a primarily a Java application with its database built by a Go program.

| Language | Framework/Library                                                                                                                                                          |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java     | Apache Commons Lang <br> Caffeine <br> JJWT <br> Logbook <br> Lombok <br> MapStruct <br> Spring Boot <br> Spring Cache <br> Spring Data JPA <br> Spring MVC <br> SpringDoc |
| Go       | modernc.org/sqlite                                                                                                                                                         |

## API Comparison

1. Stricter validation constraints are placed upon the user input. 
2. HTTP 401 and HTTP 422 responses include more detailed failure information.
3. HTTP 405 is used for unsupported HTTP methods instead of HTTP 404.
4. HTTP 415 is used for unsupported media types instead of returning the response in a different format.
5. A white label error page is returned when the URL includes invalid characters.

6. `POST /auth/login`
   - Different cookie attributes and header/value pairs are sent due to the inner workings of Spring.

7. `GET /dogs/search`
   - Maximum number of dogs can include as much as the total number of rows in database.
   - "prev" and "next" fields are not included in the response when their value of "from" parameter is out of bounds.
     - For "prev," it is out of bounds when "from" is less than 0.
     - For "next," it is out of bounds when "from" is equal to or greater than the total number of result.
   - Order of parameters in "next" and "prev" fields can be different sometimes.
   - Dog IDs can be listed differently due to data insertion order and database sorting.
   - Can pass in multiple values for a query string parameter with `,`.
     - Must list out the values one by one for each parameter in the original implementation. 
   - HTTP 422 instead of HTTP 400/500 is used when an out of range value is provided for some query string parameters.

8. `POST /dogs`
   - Fields are sorted by their names in alphabetical order.
   - A nonexistent dog ID is omitted from the response.

9. `GET /status`
   - A new health check endpoint that is missing from the original.
