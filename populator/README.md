# Populator

Populator is a tool for creating the initial data for the back end. It fetches data from Fetch Rewards' API and stores it in a SQLite database.

## Getting Started

Please make sure you have `make` and `Go` (v1.24+) installed on your system.

1. Run `make` to build and run the program.
   - The database file is named `dogs.db` and is stored in the current working directory.
2. When you are done, move the database file out of the folder and run `make clean` to remove the binary.

The login token expires after an hour. Populator does not handle it because running it should not take more than an hour to do so.

## Database Schema

SQLite will accept any type of data for the fields despite the data type parameters in the `CREATE TABLE` statement. As a result, `CHECK` constraints are needed for each field declared.

```sql
CREATE TABLE Dog (
   age INTEGER NOT NULL CHECK (typeof(age) = 'integer'),
   breed TEXT NOT NULL CHECK (typeof(breed) = 'text'),
   id TEXT NOT NULL CHECK (typeof(id) = 'text'),
   image_link TEXT NOT NULL CHECK (typeof(image_link) = 'text'),
   name TEXT NOT NULL CHECK (typeof(name) = 'text'),
   zip_code TEXT NOT NULL CHECK (typeof(zip_code) = 'text'),
   CONSTRAINT DogPrimaryKey PRIMARY KEY (id)
)
```