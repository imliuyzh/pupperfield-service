# Populator

Populator is a tool for creating the initial data for the back end. It fetches data from Fetch Rewards' API and stores it in a SQLite database.

## Getting Started

Please make sure you have `make` and Go (v1.24+) installed on your system.

1. Run `make` to build and run the program.
   - The database file is named `dogs.db` and is stored in the current working directory.
2. When you are done, move the database file out of the folder and run `make clean` to remove the binary.

## Schema

Populator will create a table with the schema below.

```sql
CREATE TABLE Dog (
   age INTEGER NOT NULL,
   breed TEXT NOT NULL,
   id TEXT NOT NULL,
   image_link TEXT NOT NULL,
   name TEXT NOT NULL,
   zip_code TEXT NOT NULL,
   CONSTRAINT DogPrimaryKey PRIMARY KEY (id)
) STRICT;
```
