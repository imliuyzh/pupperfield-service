// Populator crawls Fetch Rewards' API and stores its data in a SQLite file.
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
func sendRequest(client *http.Client, request *http.Request) ([]byte, error) {
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
		return nil, fmt.Errorf("failed to read response body: %w", err)
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

	_, err = sendRequest(client, request)
	return err
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
	err = json.Unmarshal(response, &breeds)
	return breeds, err
}

// createDatabase initializes the database file "dogs.db" with a schema
// (see README.md). It returns the database and an error if any occurs.
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
	return database, err
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
	err = json.Unmarshal(response, &dogs)
	return dogs, err
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

// insertDogsByBreed puts the dogs passed in into the database in one
// transaction. It returns an error when there is one.
func insertDogsByBreed(database *sql.DB, dogs []dog) error {
	transaction, err := database.Begin()
	if err != nil {
		return err
	}

	statement, err := transaction.Prepare(
		"INSERT INTO Dog (age, breed, id, image_link, name, zip_code) " +
			"VALUES (?, ?, ?, ?, ?, ?);",
	)
	if err != nil {
		return err
	}
	defer statement.Close()

	for _, dog := range dogs {
		_, err := statement.Exec(
			dog.Age, dog.Breed, dog.ID, dog.ImageLink, dog.Name, dog.ZipCode,
		)
		if err != nil {
			return err
		}
	}

	return transaction.Commit()
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
	database, err := createDatabase()
	if err != nil {
		return fmt.Errorf("createDatabase failed: %w", err)
	}
	if err = getAndInsertDogs(client, database, breeds); err != nil {
		return fmt.Errorf("getAndInsertDogs failed: %w", err)
	}

	return nil
}

// main displays date and time with each message and executes the main logic.
func main() {
	log.SetFlags(log.LstdFlags)
	if err := run(); err != nil {
		log.Fatalln(err)
	}
}
