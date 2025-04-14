// Populator gets Fetch Rewards' API data and stores it in a SQLite database.
package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"math/rand/v2"
	"net/http"
	"net/http/cookiejar"
	"net/url"
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
			age INTEGER NOT NULL CHECK (typeof(age) = 'integer'),
			breed TEXT NOT NULL CHECK (typeof(breed) = 'text'),
			id TEXT NOT NULL CHECK (typeof(id) = 'text'),
			image_link TEXT NOT NULL CHECK (typeof(image_link) = 'text'),
			name TEXT NOT NULL CHECK (typeof(name) = 'text'),
			zip_code TEXT NOT NULL CHECK (typeof(zip_code) = 'text'),
			CONSTRAINT DogPrimaryKey PRIMARY KEY (id)
		)`,
	)
	if err != nil {
		return nil, err
	}
	return database, nil
}

// login returns a pointer to http.Client that has been populated with
// "fetch-access-token." An error is returned if the request fails.
// Be aware that this token invalidates after an hour. Refresh is not
// implemented because the entire run generally takes under 15 minutes.
func login() (*http.Client, error) {
	jar, err := cookiejar.New(nil)
	if err != nil {
		return nil, err
	}

	request, err := http.NewRequest(
		"POST",
		baseURL+"/auth/login",
		bytes.NewBuffer([]byte(`{"name":"temp","email":"temp@email.com"}`)),
	)
	if err != nil {
		return nil, err
	}

	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Credentials", "include")

	client := &http.Client{Jar: jar}
	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()

	return client, nil
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

	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()

	var breeds []string
	if err = json.NewDecoder(response.Body).Decode(&breeds); err != nil {
		return nil, err
	}
	return breeds, nil
}

// getDogIDs returns a slice of dog ids for a specific breed.
// An error is returned if the request fails.
func getDogIDs(client *http.Client, breed string, from int) ([]string, error) {
	request, err := http.NewRequest(
		"GET",
		fmt.Sprintf(
			"%s/dogs/search?breeds=%s&from=%d&size=100",
			baseURL,
			url.QueryEscape(breed),
			from,
		),
		nil,
	)
	if err != nil {
		return nil, err
	}
	request.Header.Set("Credentials", "include")

	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()

	var info searchResponse
	if err = json.NewDecoder(response.Body).Decode(&info); err != nil {
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
		bytes.NewBuffer(ids),
	)
	if err != nil {
		return nil, err
	}
	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Credentials", "include")

	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()

	var dogs []dog
	if err = json.NewDecoder(response.Body).Decode(&dogs); err != nil {
		return nil, err
	}
	return dogs, nil
}

// getDogs retrieves all the dogs for a specific breed from Fetch Rewards.
// It returns a slice of dog structs and an error if any occurs. Note that
// there is a pause of up to 3 seconds between each request loop.
func getDogs(client *http.Client, breed string) ([]dog, error) {
	result := make([]dog, 0, 300)
	from := 0
	for {
		dogIDs, err := getDogIDs(client, breed, from)
		if err != nil {
			return nil, err
		}
		if len(dogIDs) <= 0 {
			break
		}

		data, err := getDogInfo(client, dogIDs)
		if err != nil {
			return nil, err
		}
		result = append(result, data...)

		from += 100
		time.Sleep(time.Second * time.Duration(rand.IntN(3)+1))
	}
	return result, nil
}

// insertDogs batch appends a sequence of dogs into the database.
// Any errors while inserting are returned.
func insertDogs(database *sql.DB, dogs []dog) error {
	context, err := database.Begin()
	if err != nil {
		return err
	}

	statement, err := context.Prepare(
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
	if err = context.Commit(); err != nil {
		return err
	}
	return nil
}

// run sets up the database by getting the data from Fetch Rewards' API.
// An error is returned if the process fails. Note the database file is
// not closed manually because it is unnecessary according to documentation.
func run() error {
	log.Println("database creation began")
	database, err := createDatabase()
	if err != nil {
		return fmt.Errorf("creating database failed: %w", err)
	}

	client, err := login()
	if err != nil {
		return fmt.Errorf("login failed: %w", err)
	}

	breeds, err := getBreeds(client)
	if err != nil {
		return fmt.Errorf("getBreeds failed: %w", err)
	}

	for _, breed := range breeds {
		log.Println("getDogs started for", breed)
		dogs, err := getDogs(client, breed)
		if err != nil {
			return fmt.Errorf("getDogs for %s failed: %w", breed, err)
		}

		log.Println("record insertion started for", breed)
		if err = insertDogs(database, dogs); err != nil {
			return fmt.Errorf("inserting %s failed: %w", breed, err)
		}
	}
	log.Println("completed")
	return nil
}

// main logs the date and time for each message and prints any errors.
func main() {
	log.SetFlags(log.LstdFlags)
	if err := run(); err != nil {
		log.Println("populating database failed:", err)
	}
}
