# Account schema

# --- !Ups

CREATE TABLE records (
    id         INTEGER NOT NULL PRIMARY KEY,
    name       TEXT NOT NULL UNIQUE
);

# --- !Downs

DROP TABLE records;
