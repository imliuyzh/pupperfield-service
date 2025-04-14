// Populator gets Fetch Rewards' API data and stores it in a SQLite database.
package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"log"
	"math/rand/v2"
	"net/http"
	"net/http/cookiejar"
	"net/url"
	"strconv"
	"strings"
	"time"

	_ "modernc.org/sqlite"
)

// dog represents a dog record returned from Fetch Rewards' API.
type dog struct {
	Age     uint8  `json:"age"`
	Breed   string `json:"breed"`
	ID      string `json:"id"`
	Image   string `json:"img"`
	Name    string `json:"name"`
	ZipCode string `json:"zip_code"`
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
// Be aware that this token invalidates after an hour.
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
	err = json.NewDecoder(response.Body).Decode(&breeds)
	if err != nil {
		return nil, err
	}
	return breeds, nil
}

// getDogIDs returns a slice of dog ids for a specific breed.
// An error is returned if the request fails.
func getDogIDs(client *http.Client, breed string, from int) ([]string, error) {
	parameters := url.Values{
		"breeds": []string{breed},
		"from":   []string{strconv.Itoa(from)},
		"size":   []string{"100"},
	}
	request, err := http.NewRequest(
		"GET",
		strings.Join(
			[]string{baseURL, "/dogs/search?", parameters.Encode()},
			"",
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
	err = json.NewDecoder(response.Body).Decode(&info)
	if err != nil {
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
	err = json.NewDecoder(response.Body).Decode(&dogs)
	if err != nil {
		return nil, err
	}
	return dogs, nil
}

// getDogs retrieves all the dogs for a specific breed from Fetch Rewards.
// It returns a slice of dog structs and an error if any occurs.
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
	defer statement.Close()
	for _, dog := range dogs {
		_, err := statement.Exec(
			dog.Age, dog.Breed, dog.ID, dog.Image, dog.Name, dog.ZipCode,
		)
		if err != nil {
			return err
		}
	}

	err = context.Commit()
	if err != nil {
		return err
	}

	return nil
}

// main sets up the database by getting the data from Fetch Rewards' API.
// Note the database file is not closed manually because it is unnecessary
// according to the documentation. Another thing is that token refresh is not
// handled because the entire run generally takes under 15 minutes.
func main() {
	log.Println("database creation began")
	database, err := createDatabase()
	if err != nil {
		log.Fatalln("creating database failed:", err)
	}

	client, err := login()
	if err != nil {
		log.Fatalln("login failed:", err)
	}

	breeds, err := getBreeds(client)
	if err != nil {
		log.Fatalln("getBreeds failed:", err)
	}

	for _, breed := range breeds {
		log.Println("getDogs started for", breed)
		dogs, err := getDogs(client, breed)
		if err != nil {
			log.Fatalf("getDogs for %s failed: %v\n", breed, err)
		}

		log.Println("record insertion started for", breed)
		err = insertDogs(database, dogs)
		if err != nil {
			log.Fatalf("inserting %s failed: %v\n", breed, err)
		}
	}
	log.Println("completed")
}

// init includes the date and time for each message.
func init() {
	log.SetFlags(log.LstdFlags)
}
