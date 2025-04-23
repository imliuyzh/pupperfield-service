// Populator is a spider that crawls Fetch Rewards' API and stores its data in
// a SQLite database file.
package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand/v2"
	"net/http"
	"net/http/cookiejar"
	"net/url"
	"strings"
	"time"

	_ "modernc.org/sqlite"
)

// dog represents a dog record returned from Fetch Rewards' API.
type dog struct {
	Age       uint8  `json:"age"`
	Breed     string `json:"breed"`
	ID        string `json:"id"`
	ImageLink string `json:"img"`
	Name      string `json:"name"`
	ZipCode   string `json:"zip_code"`
}

// searchResponse represents a response from Fetch Rewards' API.
type searchResponse struct {
	Next     string   `json:"-"`
	Previous string   `json:"-"`
	Result   []string `json:"resultIds"`
	Total    uint16   `json:"-"`
}

// baseURL points to the address of Fetch Rewards' API.
const baseURL string = "https://frontend-take-home-service.fetch.com"

// createClient returns a pointer to http.Client along with a cookie jar in it.
// An error is returned if the request fails.
func createClient() (*http.Client, error) {
	jar, err := cookiejar.New(nil)
	if err != nil {
		return nil, err
	}
	return &http.Client{Jar: jar}, nil
}

// sendRequest returns the response body as a byte slice. An error is
// returned if the request fails, the status code is not between 200
// and 299, or the body cannot be read.
func sendRequest(
	client *http.Client,
	request *http.Request,
) ([]byte, error) {
	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()

	if code := response.StatusCode; code < 200 || code > 299 {
		return nil, fmt.Errorf("unexpected status code %d", code)
	}

	body, err := io.ReadAll(response.Body)
	if err != nil {
		return nil, fmt.Errorf(
			"unexpected error while getting response body: %w", err,
		)
	}
	return body, nil
}

// login creates an entry in the client's cookie jar that has the name
// "fetch-access-token." An error is returned if the request fails.
// Be aware that this token invalidates after an hour. Refresh is not
// implemented because the entire run generally takes under 15 minutes.
func login(client *http.Client) error {
	request, err := http.NewRequest(
		"POST",
		baseURL+"/auth/login",
		strings.NewReader(`{"name":"temp","email":"temp@email.com"}`),
	)
	if err != nil {
		return err
	}
	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Credentials", "include")

	if _, err := sendRequest(client, request); err != nil {
		return err
	}
	return nil
}

// getBreeds returns a sequence of dog breeds from Fetch Rewards' API.
// An error is returned if the request fails.
func getBreeds(client *http.Client) ([]string, error) {
	request, err := http.NewRequest(
		"GET",
		baseURL+"/dogs/breeds",
		nil,
	)
	if err != nil {
		return nil, err
	}
	request.Header.Set("Credentials", "include")

	response, err := sendRequest(client, request)
	if err != nil {
		return nil, err
	}

	var breeds []string
	if err = json.Unmarshal(response, &breeds); err != nil {
		return nil, err
	}
	return breeds, nil
}

// createDatabase initializes a database file named "dogs.db"
// and creates a table named "Dog." It returns the database and
// an error if any occurs.
func createDatabase() (*sql.DB, error) {
	database, err := sql.Open("sqlite", "dogs.db")
	if err != nil {
		return nil, err
	}
	_, err = database.Exec(
		`CREATE TABLE Dog (
			age INTEGER NOT NULL,
			breed TEXT NOT NULL,
			id TEXT NOT NULL,
			image_link TEXT NOT NULL,
			name TEXT NOT NULL,
			zip_code TEXT NOT NULL,
			CONSTRAINT DogPrimaryKey PRIMARY KEY (id)
		) STRICT;`,
	)
	if err != nil {
		return nil, err
	}
	return database, nil
}

// getDogIDs returns a slice of IDs for dogs of a specific breed.
// An error is returned if the request fails.
func getDogIDs(client *http.Client, breed string, from int) ([]string, error) {
	request, err := http.NewRequest(
		"GET",
		fmt.Sprintf(
			"%s/dogs/search?breeds=%s&from=%d&size=100",
			baseURL, url.QueryEscape(breed), from,
		),
		nil,
	)
	if err != nil {
		return nil, err
	}
	request.Header.Set("Credentials", "include")

	response, err := sendRequest(client, request)
	if err != nil {
		return nil, err
	}

	var info searchResponse
	if err = json.Unmarshal(response, &info); err != nil {
		return nil, err
	}
	return info.Result, nil
}

// getDogInfo retrieves information about each dog from Fetch Rewards.
// It returns a slice of dog structs and an error if the request fails.
func getDogInfo(client *http.Client, dogIDs []string) ([]dog, error) {
	ids, err := json.Marshal(dogIDs)
	if err != nil {
		return nil, err
	}

	request, err := http.NewRequest(
		"POST",
		baseURL+"/dogs",
		strings.NewReader(string(ids)),
	)
	if err != nil {
		return nil, err
	}
	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Credentials", "include")

	response, err := sendRequest(client, request)
	if err != nil {
		return nil, err
	}

	var dogs []dog
	if err = json.Unmarshal(response, &dogs); err != nil {
		return nil, err
	}
	return dogs, nil
}

// getDogsByBreed retrieves all the dogs for a specific breed from Fetch
// Rewards. It returns a slice of dog structs and an error if any occurs.
// Note that there is a pause of up to 3 seconds between each request loop.
func getDogsByBreed(client *http.Client, breed string) ([]dog, error) {
	result := make([]dog, 0, 300)
	from := 0
	for {
		dogIDs, err := getDogIDs(client, breed, from)
		if err != nil {
			return nil, fmt.Errorf("getDogIDs failed: %w", err)
		}
		if len(dogIDs) <= 0 {
			break
		}

		data, err := getDogInfo(client, dogIDs)
		if err != nil {
			return nil, fmt.Errorf("getDogInfo failed: %w", err)
		}
		result = append(result, data...)

		from += 100
		time.Sleep(time.Second * time.Duration(rand.IntN(3)+1))
	}
	return result, nil
}

// insertDogsByBreed builds a query to put the dogs into the database.
// If any error occurs, it will be returned.
func insertDogsByBreed(database *sql.DB, dogs []dog) error {
	var query strings.Builder
	query.WriteString("INSERT INTO Dog VALUES ")
	for index, dog := range dogs {
		query.WriteString(fmt.Sprintf(
			`(%d, "%s", "%s", "%s", "%s", "%s")`,
			dog.Age, dog.Breed, dog.ID, dog.ImageLink, dog.Name, dog.ZipCode,
		))
		if index < len(dogs)-1 {
			query.WriteString(", ")
		}
	}
	if _, err := database.Exec(query.String()); err != nil {
		return err
	}
	return nil
}

// getAndInsertDogs fetches data by dog breed and puts them into the database.
// It returns an error if the process fails.
func getAndInsertDogs(
	client *http.Client,
	database *sql.DB,
	breeds []string,
) error {
	for _, breed := range breeds {
		log.Println("getDogsByBreed started for", breed)
		dogs, err := getDogsByBreed(client, breed)
		if err != nil {
			return fmt.Errorf("getDogsByBreed for %s failed: %w", breed, err)
		}
		log.Println("insertDogsByBreed started for", breed)
		if err = insertDogsByBreed(database, dogs); err != nil {
			return fmt.Errorf("insertDogsByBreed %s failed: %w", breed, err)
		}
	}
	return nil
}

// run gets the data from Fetch Rewards' API to set up the database.
// An error is returned if the process fails.
func run() error {
	client, err := createClient()
	if err != nil {
		return fmt.Errorf("createClient failed: %w", err)
	}

	if err = login(client); err != nil {
		return fmt.Errorf("login failed: %w", err)
	}

	breeds, err := getBreeds(client)
	if err != nil {
		return fmt.Errorf("getBreeds failed: %w", err)
	}

	// Note the database is not manually closed because it is unnecessary
	// according to documentation.
	log.Println("database creation began")
	database, err := createDatabase()
	if err != nil {
		return fmt.Errorf("creating database failed: %w", err)
	}
	if err = getAndInsertDogs(client, database, breeds); err != nil {
		return fmt.Errorf("getAndInsertDogs failed: %w", err)
	}
	log.Println("database creation completed")
	return nil
}

// main displays each message with date and time in the console.
func main() {
	log.SetFlags(log.LstdFlags)
	if err := run(); err != nil {
		log.Fatalln(err)
	}
}
