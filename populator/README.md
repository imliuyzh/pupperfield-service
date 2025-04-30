# Populator

Populator is a tool to create the initial data for Pupperfield's back end service. It takes data from Fetch Rewards' API and stores it in a SQLite database.

## Getting Started

Please make sure you have `make` and Go (v1.24+) installed on your system.

1. Run `make` to build and run the program.
2. When you are done, move the database file `dogs.db` to `/src/main/resources/database` and run `make clean` to remove the compiled binary.

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
) STRICT, WITHOUT ROWID;
```
