# Pupperfield Backend

`./mvnw spring-boot:run -Dspring-boot.run.profiles=localhost`

## API Reference

### Authentication

You will need to hit the login endpoint in order to access other endpoints. A successful request to the login endpoint will return an auth cookie included in the set-cookie response header. It’s an HttpOnly cookie, so you will not be able to access this value from any Javascript code (nor should you need to). Your browser will automatically send this cookie with all successive credentialed requests to the API. Note that you will need to pass a config option in order to send credentials (cookies) with each request. Some documentation to help you with this:

- Including credentials with fetch (set credentials: 'include' in request config)
- Including credentials with axios (set withCredentials: true in request config)

Postman will do this for you automatically.

### POST /auth/login

#### Body Parameters

* name - the user’s name
* email - the user’s email

#### Example

```typescript
// API Request Function
...
body: {
    name: string,
    email: string
}
...
```

#### Response

##### 200 OK

An auth cookie, fetch-access-token, will be included in the response headers. This will expire in 1 hour.

### POST /auth/logout

Hit this endpoint to end a user’s session. This will invalidate the auth cookie.

### GET /dogs/breeds

#### Return Value

Returns an array of all possible breed names.

### GET /dogs/search

#### Query Parameters

The following query parameters can be supplied to filter the search results. All are optional; if none are provided, the search will match all dogs.

* breeds - an array of breeds
* zipCodes - an array of zip codes
* ageMin - a minimum age
* ageMax - a maximum age

Additionally, the following query parameters can be used to configure the search:

* size - the number of results to return; defaults to 25 if omitted
* from - a cursor to be used when paginating results (optional)
* sort - the field by which to sort results, and the direction of the sort; in the format sort=field:[asc|desc].
  * results can be sorted by the following fields: breed, name, and age
  * ex: sort=breed:asc

Results should be sorted alphabetically by breed by default. Users should be able to modify this sort to be ascending or descending.

#### Return Value

Returns an object with the following properties:

* resultIds - an array of dog IDs matching your query
* total - the total number of results for the query (not just the current page)
* next - a query to request the next page of results (if one exists)
* prev - a query to request the previous page of results (if one exists)

The maximum total number of dogs that will be matched by a single query is 10,000.

### POST /dogs

#### Body Parameters

The body should be an array of no more than 100 dog IDs to fetch (no pun intended).

```typescript
// API Request Function
...
body: string[]
...
```

#### Return Value

Returns an array of dog objects

### POST /dogs/match

#### Body Parameters

The body of this request should be an array of dog IDs.

#### Example

```typescript
// API Request Function
...
body: string[]
...
```

#### Return Value

This endpoint will select a single ID from the provided list of dog IDs. This ID represents the dog the user has been matched with for adoption.

```typescript
interface Match {
    match: string
}
```
